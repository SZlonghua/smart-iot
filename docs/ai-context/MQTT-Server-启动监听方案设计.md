# MQTT Server 启动监听方案设计

## Context

当前 MQTT 协议栈处于骨架阶段：`VertxMqttServerNetwork`、`MqttConnection`、`MqttServerDeviceGateway.doStartup/doShutdown` 均为空实现；无 Vert.x 依赖。需要完成 MQTT Broker 的完整启动监听链路：接收设备连接 → 认证 → 会话管理 → 协议编解码 → 消息路由到事件总线。

---

## 修改要点

1. **Vertx 由 Spring 统一管理** — 新建 `VertxAutoConfiguration` 创建共享 Vertx bean，`MqttServerNetworkProvider` 通过构造器注入
2. **编解码不自己实现** — 直接调用 `ProtocolSupport.getMessageCodec(Transport)` 获取协议自带编解码器，`codec.encode/decode` 完成编解码
3. **Endpoint 全程链式处理** — 从接收 endpoint 到认证、会话、accept、init、消息路由，全部走 `Mono`/`Flux` 链式调用

---

## 1. 依赖引入 (sa-base/pom.xml)

```xml
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-mqtt</artifactId>
</dependency>
<dependency>
    <groupId>io.vertx</groupId>
    <artifactId>vertx-core</artifactId>
</dependency>
```

---

## 2. VertxAutoConfiguration (新建 — 所有协议公用)

`module/support/protocol/config/VertxAutoConfiguration.java` — 所有协议（MQTT、TCP、HTTP、CoAP、WebSocket、UDP）共享同一个 Vertx 实例：

```java
@Configuration
public class VertxAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "vertx")
    public VertxOptions vertxOptions() {
        return new VertxOptions();
    }

    @Bean(destroyMethod = "close")
    public Vertx vertx(VertxOptions vertxOptions) {
        return Vertx.vertx(vertxOptions);
    }
}
```

所有需要 Vertx 的组件（Provider、Network、Connection）统一通过构造器注入这个 Bean。

---

## 3. EncodedMessage / MqttEncodedMessage 落地

### 3.1 EncodedMessage 增加方法

`common/message/raw/EncodedMessage.java`：

```java
public interface EncodedMessage {

    @Nonnull
    ByteBuf getPayload();

    default String payloadAsString() {
        return getPayload().toString(StandardCharsets.UTF_8);
    }

    default JSONObject payloadAsJson() {
        return (JSONObject) JSON.parse(payloadAsBytes());
    }

    default JSONArray payloadAsJsonArray() {
        return (JSONArray) JSON.parse(payloadAsBytes());
    }

    default byte[] payloadAsBytes() {
        return ByteBufUtil.getBytes(getPayload());
    }

    @Deprecated
    default byte[] getBytes() {
        return ByteBufUtil.getBytes(getPayload());
    }

    default byte[] getBytes(int offset, int len) {
        return ByteBufUtil.getBytes(getPayload(), offset, len);
    }
}
```

### 3.2 MqttEncodedMessage 增加方法

`module/support/protocol/mqtt/message/MqttEncodedMessage.java`：

```java
public interface MqttEncodedMessage extends EncodedMessage {
    String getClientId();
    int getMessageId();
    String getTopic();
    int getQos();
}
```

### 3.3 DefaultMqttEncodedMessage (新建)

`module/support/protocol/mqtt/message/DefaultMqttEncodedMessage.java` — 实现 MqttEncodedMessage，内部持有 vertx `MqttPublishMessage`，getter 均委托给 vertx：

```java
@Getter
public class DefaultMqttEncodedMessage implements MqttEncodedMessage {
    private final ByteBuf payload;
    private final MqttPublishMessage delegate;

    public DefaultMqttEncodedMessage(MqttPublishMessage publishMessage) {
        this.payload = publishMessage.payload();
        this.delegate = publishMessage;
    }

    @Override
    public String getClientId() {
        return delegate.clientIdentifier();
    }

    @Override
    public int getMessageId() {
        return delegate.messageId();
    }

    @Override
    public String getTopic() {
        return delegate.topicName();
    }

    @Override
    public int getQos() {
        return delegate.qosLevel().value();
    }
}
```

---

## 4. MqttServerNetworkProvider 改造

`module/support/protocol/mqtt/network/MqttServerNetworkProvider.java`

**关键变化：构造器注入 Vertx**

```java
@Slf4j
public class MqttServerNetworkProvider implements NetworkProvider<MqttServerConfig> {

    private final Vertx vertx;

    public MqttServerNetworkProvider(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public String getId() { return "mqtt-server"; }

    @Override
    public NetworkType getType() { return DefaultNetworkType.MQTT_SERVER; }

    @Override
    public Mono<MqttServerConfig> createConfig(@Nonnull NetworkProperties properties) {
        MqttServerConfig cfg = new MqttServerConfig();
        BeanUtil.copyProperties(properties.getConfigurations(), cfg);
        cfg.setId(properties.getId());
        return Mono.just(cfg);
    }

    @Override
    public Mono<Network> createNetwork(@Nonnull MqttServerConfig config) {
        return Mono.just(new VertxMqttServerNetwork(config, vertx));
    }

    @Override
    public Mono<Network> reload(@Nonnull Network network, @Nonnull MqttServerConfig config) {
        VertxMqttServerNetwork mqttNetwork = (VertxMqttServerNetwork) network;
        mqttNetwork.reload(config);
        return Mono.just(network);
    }
}
```

**MqttNetworkComponentConfig 同步调整** — `new MqttServerNetworkProvider(vertx())` 改为注入 `Vertx` Bean。

---

## 5. VertxMqttServerNetwork 内部实现

`module/support/protocol/mqtt/network/VertxMqttServerNetwork.java`

```java
@Slf4j
public class VertxMqttServerNetwork implements MqttServerNetwork {
    private final String id;
    @Setter
    private MqttServerConfig config;
    private final Vertx vertx;
    private volatile MqttServer mqttServer;
    private volatile boolean alive;
    private volatile Consumer<VertxMqttConnection> connectionListener;

    public VertxMqttServerNetwork(MqttServerConfig config, Vertx vertx) {
        this.id = config.getId();
        this.config = config;
        this.vertx = vertx;
        this.mqttServer = createMqttServer(config);
    }

    @Override public String getId() { return id; }
    @Override public NetworkType getType() { return DefaultNetworkType.MQTT_SERVER; }

    @Override
    public void start() {
        mqttServer.endpointHandler(endpoint -> {
            if (connectionListener != null) connectionListener.accept(new VertxMqttConnection(endpoint));
        }).listen(ar -> {
            if (ar.succeeded()) { alive = true; log.info("MQTT server started on {}", config.getLocalPort()); }
            else { log.error("MQTT server start failed", ar.cause()); }
        });
    }

    @Override public void shutdown() {
        alive = false;
        if (mqttServer != null) mqttServer.close();
    }

    /** 内部 reload — 关闭旧 MqttServer，用新 config 重建 MqttServer，外部会重新 start() */
    public void reload(MqttServerConfig newConfig) {
        if (mqttServer != null) mqttServer.close();
        this.config = newConfig;
        this.mqttServer = createMqttServer(newConfig);
    }

    private MqttServer createMqttServer(MqttServerConfig config) {
        MqttServerOptions options = new MqttServerOptions()
            .setHost(config.getLocalAddress())
            .setPort(config.getLocalPort())
            .setMaxMessageSize(config.getMaxMessageSize());
        return vertx.createMqttServer(options);
    }

    @Override public boolean isAlive() { return alive; }
    @Override public boolean isAutoReload() { return true; }

    public void onConnection(Consumer<VertxMqttConnection> listener) {
        this.connectionListener = listener;
    }
}
```

**MqttServerNetwork 接口同步扩展** — 增加 `void onConnection(Consumer<VertxMqttConnection> listener)` 方法。

---

## 6. VertxMqttConnection 实现 (新建)

`module/support/protocol/mqtt/session/VertxMqttConnection.java` — 实现 `MqttConnection` (extends `ClientConnection`)：

```java
@Slf4j
public class VertxMqttConnection implements MqttConnection {
    private final MqttEndpoint endpoint;
    private final AtomicBoolean alive = new AtomicBoolean(true);
    private final Sinks.Many<EncodedMessage> messageSink =
        Sinks.many().multicast().onBackpressureBuffer();
    private Runnable disconnectCallback;

    @Override public Flux<EncodedMessage> receiveMessage() {
        return messageSink.asFlux();
    }

    @Override public Mono<Void> sendMessage(EncodedMessage msg) { ... }

    @Override public void disconnect() { endpoint.close(); alive.set(false); }

    public String getClientId() { return endpoint.clientIdentifier(); }

    @Override public boolean isAlive() { return alive.get() && endpoint.isConnected(); }

    // ===== 连接生命周期 =====

    /** accept — 回复 CONNACK 接受连接 */
    public void accept() { endpoint.accept(false); }

    /** reject — 回复 CONNACK 拒绝连接 */
    public void reject(int returnCode) {
        endpoint.reject((byte) returnCode); alive.set(false);
        endpoint.close();
    }

    /** init — 链式注册全部 MQTT 包处理器 */
    public VertxMqttConnection init() {
        endpoint
            .pingHandler(v -> {})
            .publishHandler(msg -> {
                if (msg.qosLevel() == QoS.AT_LEAST_ONCE)
                    endpoint.publishAcknowledge(msg.messageId());
                if (msg.qosLevel() == QoS.EXACTLY_ONCE)
                    endpoint.publishReceived(msg.messageId());
                messageSink.tryEmitNext(new DefaultMqttEncodedMessage(msg));
            })
            .publishAcknowledgeHandler(id ->
                log.debug("PUBACK messageId={}", id))
            .publishReceivedHandler(id ->
                endpoint.publishRelease(id))
            .publishReleaseHandler(id ->
                endpoint.publishComplete(id))
            .publishCompletionHandler(id ->
                log.debug("PUBCOMP messageId={}", id))
            .subscribeHandler(sub -> {
                List<MqttQoS> grantedQos = sub.topicSubscriptions().stream()
                    .map(s -> MqttQoS.valueOf(s.qualityOfService().value()))
                    .collect(toList());
                endpoint.subscribeAcknowledge(sub.messageId(), grantedQos);
            })
            .unsubscribeHandler(unsub ->
                endpoint.unsubscribeAcknowledge(unsub.messageId()))
            .closeHandler(v -> {
                alive.set(false);
                messageSink.tryEmitComplete();
                if (disconnectCallback != null) disconnectCallback.run();
            })
            .exceptionHandler(e ->
                log.error("MQTT connection error — clientId={}", endpoint.clientIdentifier(), e));
        return this;
    }

    @Override
    public void onDisconnect(Runnable callback) { this.disconnectCallback = callback; }
}
```

---

## 7. MqttServerDeviceGateway 改造

`module/support/protocol/mqtt/gateway/MqttServerDeviceGateway.java`

### 7.1 doStartup — 全程链式处理 endpoint

```
endpoint 到达
  → Mono.just(endpoint)
  → filter(网关状态==started, 否则reject CONNECTION_REFUSED_SERVER_UNAVAILABLE)
  → flatMap(构建 VertxMqttConnection)
  → flatMap(提取 clientId/password, 构建 AuthenticationRequest)
  → flatMap(调用 protocolSupport.authenticate()) — 认证失败 reject NOT_AUTHORIZED
  → flatMap(通过 sessionManager.compute() 获取/替换会话)
  → doOnNext(connection.accept()) — 回复 CONNACK
  → flatMap(connection.init())
  → flatMap(connection.receiveMessage()
        → codec.decode(context) — 调用协议编解码器
        → messageHandler.handle(message)
    )
  → doOnError(关闭连接)
  → subscribe()
```

示例伪代码：

```java
@Override
protected Mono<Void> doStartup() {
    return Mono.just(mqttServerNetwork)
            .doOnNext(net -> net.onConnection(this::handleConnection))
            .doOnNext(MqttServerNetwork::start)
            .then();
}

/**
 * 处理设备连接 — 全程链式：
 * 状态判定 → 认证 → 会话 → accept → init → 消息流
 */
private void handleConnection(VertxMqttConnection conn) {
    Mono.just(conn)
        // 1. 判定网关状态
        .filter(c -> getState() == GatewayState.started)
        .switchIfEmpty(Mono.fromRunnable(() ->
            conn.reject(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE))
            .then(Mono.empty()))
        // 2. 设备认证
        .flatMap(c -> {
            AuthenticationRequest authReq = new AuthenticationRequest();
            authReq.setDeviceId(c.getClientId());
            return protocolSupport
                .flatMap(ps -> ps.authenticate(authReq, null))
                .flatMap(authResp -> {
                    if (!authResp.isSuccess()) {
                        c.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED);
                        return Mono.empty();
                    }
                    return Mono.just(c);
                });
        })
        // 3. 会话管理 — compute 替换
        .flatMap(c -> {
            MqttConnectionSession session = new MqttConnectionSession(c.getClientId());
            session.setConnection(c);
            return sessionManager.compute(session.getDeviceId(),
                oldSession -> Mono.just(session))
                .thenReturn(c);
        })
        // 4. accept — 回复 CONNACK
        .doOnNext(VertxMqttConnection::accept)
        // 5. init — 注册所有 handler
        .doOnNext(VertxMqttConnection::init)
        // 6-7. 消息处理链 — 编解码 + 路由
        .flatMap(c ->
            c.receiveMessage()
                .flatMap(encodedMsg ->
                    protocolSupport
                        .flatMap(ps -> ps.getMessageCodec(DefaultTransport.MQTT))
                        .flatMap(codec -> codec.decode(decodeContext))
                )
                .flatMap(message -> messageHandler.handle(message))
                .doOnError(err -> {
                    log.error("消息处理异常", err);
                    c.disconnect();
                })
                .then()
        )
        // 断连清理
        .doOnTerminate(() -> {
            /* 清理会话,发布 DeviceOfflineMessage */ })
        .subscribe();
}
```

### 7.2 onMessage — 消息流

```java
// onMessage 直接用 Sinks.Many 构建的 Flux 作为网关外部消息流
// 每个 connection 的 receiveMessage() → codec.decode → sink.tryEmitNext
private final Sinks.Many<Message> gatewayMessageSink =
    Sinks.many().multicast().onBackpressureBuffer();

@Override
public Flux<Message> onMessage() {
    return gatewayMessageSink.asFlux();
}
```

### 7.3 doShutdown

```java
@Override
protected Mono<Void> doShutdown() {
    // close all connections → shutdown network
    return Mono.fromRunnable(() -> mqttServerNetwork.shutdown());
}
```

---

## 8. 协议编解码 — 直接调用 ProtocolSupport 中的编解码器

**不自己实现编解码逻辑**。Gateway 在 doStartup 中通过：

```java
// 解码：EncodedMessage → Message（在消息处理链中直接调用 protocolSupport 获取编解码器）
protocolSupport
    .flatMap(ps -> ps.getMessageCodec(DefaultTransport.MQTT))
    .flatMap(codec -> codec.decode(decodeContext))
```

获取协议自带的 `DeviceMessageCodec`，消息处理时直接调用：

```java
// 解码: EncodedMessage → Message
codec.decode(decodeContext)

// 编码: Message → EncodedMessage (下行)
codec.encode(encodeContext)
```

### DeviceMessageDecoder 签名补充

`common/message/codec/DeviceMessageDecoder.java` 当前为空接口，需增加：

```java
public interface DeviceMessageDecoder {
    @Nonnull Publisher<? extends Message> decode(@Nonnull MessageDecodeContext context);
}
```

---

## 9. DefaultDecodedClientMessageHandler 落地

`common/message/support/DefaultDecodedClientMessageHandler.java`

```java
public class DefaultDecodedClientMessageHandler implements DecodedClientMessageHandler {
    private final IEventBus eventBus;

    public Mono<Void> handle(Message message) {
        // Reply 类消息 → 回复到原始请求方
        if (message instanceof MessageReply) {
            return replyToOriginalRequest((MessageReply) message);
        }
        // 子设备消息 → 解包后重新发布
        if (message instanceof ChildDeviceMessage) {
            ChildDeviceMessage childMsg = (ChildDeviceMessage) message;
            Message unwrapped = childMsg.getChildDeviceMessage();
            eventBus.publishAsync(unwrapped);
            return Mono.empty();
        }
        // 普通消息 → 发布到事件总线
        eventBus.publishAsync(message);
        return Mono.empty();
    }
}
```

---

## 10. MqttConnectionSession 扩展

`module/support/protocol/mqtt/session/MqttConnectionSession.java` — 增加 `Connection` 引用：

```java
public class MqttConnectionSession extends AbstractDeviceSession {
    @Getter @Setter
    private VertxMqttConnection connection;

    public MqttConnectionSession(String deviceId) {
        super(deviceId, DefaultTransport.MQTT);
    }

    @Override
    public boolean isAlive() {
        return !closed && connection != null && connection.isAlive();
    }
}
```

---

## 11. 涉及文件清单

| 文件 | 操作 |
|------|------|
| `sa-base/pom.xml` | 添加 vertx-mqtt + vertx-core |
| `module/support/protocol/config/VertxAutoConfiguration.java` | **新建** — 所有协议公用 Vertx Bean |
| `mqtt/config/MqttNetworkComponentConfig.java` | 调整 Provider 构造注入 |
| `common/message/raw/EncodedMessage.java` | 增加 getPayload/getDirection |

| `mqtt/message/MqttEncodedMessage.java` | 增加 getMessageId/getTopic/getQos |
| `mqtt/message/DefaultMqttEncodedMessage.java` | **新建** — MqttEncodedMessage 实现 |
| `mqtt/network/MqttServerNetwork.java` | 增加 onConnection 方法 |
| `mqtt/network/VertxMqttServerNetwork.java` | **重写** — 真实 vertx MqttServer |
| `mqtt/network/MqttServerNetworkProvider.java` | 重写 — 构造注入 Vertx |
| `mqtt/session/VertxMqttConnection.java` | **新建** — MqttConnection 实现 |
| `mqtt/session/MqttConnectionSession.java` | 扩展 — 持有 Connection |
| `mqtt/gateway/MqttServerDeviceGateway.java` | **重写** — 链式 doStartup |
| `common/message/codec/DeviceMessageDecoder.java` | 增加 decode 方法 |
| `common/message/support/DefaultDecodedClientMessageHandler.java` | 填充 handle 逻辑 |

---

## 12. 验证方案

1. **单元测试** — VertxTestContext 模拟 MQTT 客户端连接，验证 CONNACK、publish→decode→handle→eventBus
2. **集成测试** — 启动 SpringBoot，HiveMQ Client 连接，验证认证/会话/消息收发完整链路
3. **reload 测试** — 修改网络组件配置，验证 reload 事件 → 旧连接断开 + 新连接新配置
