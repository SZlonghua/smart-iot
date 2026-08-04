# DeviceGateway — 设备网关设计文档

## 一、设计动机

IoT 平台需要同时接入多种异构设备接入方式（MQTT Broker、TCP Server、HTTP、CoAP、WebSocket 等）。每种接入方式的生命周期管理（启动/暂停/关闭）、消息收发、配置管理各不相同。

`DeviceGateway` 是平台对"设备接入网关"的统一抽象。它定义了一套与协议无关的网关接口层，上层的设备消息处理、会话管理等模块只依赖这个接口，不感知底层协议差异。

## 二、架构概览

```
┌───────────────────────────────────────────────┐
│              DeviceGatewayManager              │  ← 运行时管理（启动/关闭/重载/查找）
└──────────────────┬────────────────────────────┘
                   │ 管理
┌──────────────────▼────────────────────────────┐
│               DeviceGateway                    │  ← 网关运行时抽象
│  (id, state, onMessage: Flux<Message>,        │
│   startup/pause/shutdown)                     │
└──────────────────┬────────────────────────────┘
                   │ 创建
┌──────────────────▼────────────────────────────┐
│           DeviceGatewayProvider                │  ← SPI 工厂
│  (id, transport, createGateway/properties)    │
└──────────────────┬────────────────────────────┘
                   │ 配置
┌──────────────────▼────────────────────────────┐
│          DeviceGatewayProperties               │  ← POJO 配置
│  (id, provider, protocol, transport, status)  │
└───────────────────────────────────────────────┘

辅助接口:
- DeviceGatewayProviders         ← Provider 注册中心
- DeviceGatewayPropertiesManager ← 配置持久层（DB/文件）
- GatewayState                   ← 生命周期状态枚举
```

## 三、包结构

```
net.lab1024.sa.base.common.gateway
  ├── DeviceGateway.java                (接口：网关运行时抽象)
  ├── DeviceGatewayProvider.java         (接口：SPI 工厂)
  ├── DeviceGatewayManager.java          (接口：网关运行管理)
  ├── DeviceGatewayProviders.java        (接口：Provider 注册中心)
  ├── DeviceGatewayPropertiesManager.java(接口：配置管理)
  ├── DeviceGatewayProperties.java       (POJO：网关配置)
  └── GatewayState.java                  (枚举：生命周期状态)
```

## 四、核心接口

### 4.1 DeviceGateway — 网关运行时

```java
public interface DeviceGateway {

    String getId();

    /** 设备→平台消息流，网关关闭后流不终止（保活订阅安全） */
    Flux<Message> onMessage();

    Mono<Void> startup();     // 启动
    Mono<Void> pause();       // 暂停（停止处理消息）
    Mono<Void> shutdown();    // 关闭

    // === 状态 ===
    default boolean isAlive() { return true; }
    default boolean isStarted() { return getState() == GatewayState.started; }
    default GatewayState getState() { return GatewayState.started; }

    // === 生命周期事件 ===
    default void doOnStateChange(BiConsumer<GatewayState, GatewayState> listener) { }
    default void doOnShutdown(Disposable disposable) {
        doOnStateChange((before, after) -> {
            if (after == GatewayState.shutdown) disposable.dispose();
        });
    }
}
```

**关键设计：**
- `onMessage()` 返回 `Flux<Message>`，网关关闭后流**不终止**（不是 complete，而是无限期等待）。这样消息消费者不会因为网关重启而丢失订阅。
- 状态回调采用 `BiConsumer<GatewayState, GatewayState>`，参数是 `(before, after)`，支持监听状态迁移。
- `doOnShutdown(Disposable)` 是便捷方法：网关关闭时自动 dispose 关联资源。

### 4.2 DeviceGatewayProvider — SPI 工厂

```java
public interface DeviceGatewayProvider {

    String getId();                    // Provider 唯一标识
    String getName();                  // 展示名称
    default String getDescription() { return null; }
    default int getOrder() { return Integer.MAX_VALUE; }  // 排序

    Transport getTransport();          // 支持的传输协议

    /** 根据配置创建设备网关 */
    Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties);

    /** 重载网关（默认不重载，直接返回原实例） */
    default Mono<? extends DeviceGateway> reloadDeviceGateway(
            DeviceGateway gateway, DeviceGatewayProperties properties) {
        return Mono.just(gateway);
    }
}
```

**扩展方式：** 新协议只需实现 `DeviceGatewayProvider`，通过 `DeviceGatewayProviders.addGatewayProvider()` 注册即可。

### 4.3 DeviceGatewayManager — 运行管理

```java
public interface DeviceGatewayManager {
    Mono<DeviceGateway> getGateway(String id);     // 查找
    Mono<Void> reload(String gatewayId);           // 重载
    Mono<Void> start(String id);                   // 启动
    Mono<Void> shutdown(String gatewayId);         // 关闭

    void init();          // 注册 eventBus 监听器
    Mono<Void> loadAll(); // 加载所有已启用的网关
}
```

### 4.4 DeviceGatewayProviders — Provider 注册中心

```java
public interface DeviceGatewayProviders {
    Mono<DeviceGatewayProvider> getProvider(String provider);
    Flux<DeviceGatewayProvider> getProviders();
    Mono<Void> addGatewayProvider(DeviceGatewayProvider provider);  // 动态注册
}
```

### 4.5 DeviceGatewayPropertiesManager — 配置管理

```java
public interface DeviceGatewayPropertiesManager {
    Mono<DeviceGatewayProperties> getProperties(String id);
    Flux<DeviceGatewayProperties> getAllProperties();
}
```

### 4.6 DeviceGatewayEvent — 网关生命周期事件

sa-base 模块定义的事件，由 sa-admin 的 `GatewayEventHandler` 监听 `GatewayEntity` CRUD 事件后发布。

```java
@Getter @AllArgsConstructor(staticName = "of")
public class DeviceGatewayEvent {
    private final Type type;                          // SAVED / UPDATED / DELETED
    private final DeviceGatewayProperties properties; // 网关配置

    public enum Type { SAVED, UPDATED, DELETED }
}
```

**发布规则：**

| 操作 | 发布事件 | 说明 |
|------|---------|------|
| 新增网关 + status=1 | `SAVED` | 启用时创建并启动 |
| 新增网关 + status=0 | 不发布 | 禁用时不启动 |
| 修改网关 + status 变为 1 | `UPDATED` | 重载网关 |
| 修改网关 + status 变为 0 | `DELETED` | 关闭网关 |
| 删除网关 | `DELETED` | 关闭网关，释放资源 |

## 五、实现类

### 5.1 DefaultDeviceGatewayManager

```java
public class DefaultDeviceGatewayManager implements DeviceGatewayManager {

    private final Map<String, DeviceGateway> registry = new ConcurrentHashMap<>();
    private final DeviceGatewayProviders providers;
    private final DeviceGatewayPropertiesManager propertiesManager;
    private final IEventBus eventBus;

    public DefaultDeviceGatewayManager(DeviceGatewayProviders providers,
                                        DeviceGatewayPropertiesManager propertiesManager,
                                        IEventBus eventBus) { ... }

    @Override
    public void init() {
        eventBus.subscribe(new EventHandler<DeviceGatewayEvent>() {
            @Override
            public void handle(DeviceGatewayEvent event) {
                switch (event.getType()) {
                    case SAVED:
                        start(event.getProperties());
                        break;
                    case UPDATED:
                        reload(event.getProperties());
                        break;
                    case DELETED:
                        shutdown(event.getProperties());
                        break;
                }
            }
        });
    }

    @Override
    public Mono<Void> loadAll() {
        return propertiesManager.getAllProperties()
                .filter(p -> p.getStatus() == 1)
                .flatMap(this::start)
                .then();
    }

    private Mono<Void> start(DeviceGatewayProperties props) { ... }
    private Mono<Void> reload(DeviceGatewayProperties props) { ... }
    private Mono<Void> shutdown(DeviceGatewayProperties props) { ... }
}
```

### 5.2 GatewayEventHandler（sa-admin 模块）

监听 `GatewayEntity` 的 CRUD 事件，转化为 `DeviceGatewayEvent` 发布：

```java
@Component
public class GatewayEventHandler {

    @EventListener
    public void onSaved(SaveAfterEvent<GatewayEntity> event) {
        GatewayEntity entity = event.getAfterData();
        if (entity == null || entity.getStatus() != 1) return;  // 仅启用时发布
        eventBus.publish(DeviceGatewayEvent.of(SAVED, entity.toProperties()));
    }

    @EventListener
    public void onUpdated(UpdateAfterEvent<GatewayEntity> event) {
        GatewayEntity entity = event.getAfterData();
        if (entity == null) return;
        if (entity.getStatus() == 1) {
            eventBus.publish(DeviceGatewayEvent.of(UPDATED, entity.toProperties()));
        } else {
            eventBus.publish(DeviceGatewayEvent.of(DELETED, entity.toProperties()));
        }
    }

    @EventListener
    public void onDeleted(DeleteAfterEvent<GatewayEntity> event) {
        eventBus.publish(DeviceGatewayEvent.of(DELETED,
                DeviceGatewayProperties.builder().id(String.valueOf(event.getEntityId())).build()));
    }
}
```

### 5.3 DefaultDeviceGatewayProviders

```java
public class DefaultDeviceGatewayProviders implements DeviceGatewayProviders {
    private final Map<String, DeviceGatewayProvider> registry = new ConcurrentHashMap<>();

    public DefaultDeviceGatewayProviders(List<DeviceGatewayProvider> providerList) {
        providerList.forEach(p -> registry.put(p.getId(), p));
    }

    @Override public Mono<DeviceGatewayProvider> getProvider(String id) {
        return Mono.justOrEmpty(registry.get(id));
    }
    @Override public Flux<DeviceGatewayProvider> getProviders() {
        return Flux.fromIterable(registry.values());
    }
    @Override public Mono<Void> addGatewayProvider(DeviceGatewayProvider provider) {
        registry.put(provider.getId(), provider); return Mono.empty();
    }
}
```

### 5.4 DeviceGatewayProvider 实现类

位于 `net.lab1024.sa.base.module.support.protocol.{protocol}/gateway/`，与同协议的 session 平级：

```
net.lab1024.sa.base.module.support.protocol
  ├── tcp/
  │     ├── session/                          (会话)
  │     ├── gateway/
  │     │     ├── TcpDeviceGatewayProvider.java
  │     │     └── TcpDeviceGateway.java
  │     └── config/                           (TCP 配置)
  ├── http/
  │     ├── session/
  │     ├── gateway/
  │     │     ├── HttpServerDeviceGatewayProvider.java
  │     │     └── HttpServerDeviceGateway.java
  │     └── config/
  ├── mqtt/
  │     ├── session/
  │     ├── gateway/
  │     │     ├── MqttServerDeviceGatewayProvider.java
  │     │     ├── MqttServerDeviceGateway.java
  │     │     ├── MqttClientDeviceGatewayProvider.java
  │     │     └── MqttClientDeviceGateway.java
  │     └── config/
  ├── coap/
  │     ├── session/
  │     ├── gateway/
  │     │     ├── CoapDeviceGatewayProvider.java
  │     │     └── CoapDeviceGateway.java
  │     └── config/
  ├── websocket/
  │     ├── session/
  │     ├── gateway/
  │     │     ├── WebSocketDeviceGatewayProvider.java
  │     │     └── WebSocketDeviceGateway.java
  │     └── config/
  └── children/
        ├── session/
        └── gateway/
              ├── ChildrenDeviceGatewayProvider.java
              └── ChildrenDeviceGateway.java
```

| 实现类 | 对应协议 | 说明 |
| `TcpDeviceGatewayProvider` | TCP | 自建 TCP Server 网关 |
| `HttpServerDeviceGatewayProvider` | HTTP | HTTP 网关（轮询/长轮询模式） |
| `MqttServerDeviceGatewayProvider` | MQTT | 自建 MQTT Broker 网关 |
| `MqttClientDeviceGatewayProvider` | MQTT | 连接外部 EMQX Broker 网关 |
| `CoapDeviceGatewayProvider` | CoAP | CoAP 网关 |
| `WebSocketDeviceGatewayProvider` | WebSocket | WebSocket 网关 |
| `ChildrenDeviceGatewayProvider` | — | 子设备网关（不持有网络连接，通过父网关代理） |

```java
// 示例：MQTT Server 网关 Provider
public class MqttServerDeviceGatewayProvider implements DeviceGatewayProvider {

    private final DeviceRegistry registry;
    private final DeviceSessionManager sessionManager;
    private final ProtocolSupports protocolSupports;
    private final NetworkManager networkManager;

    public MqttServerDeviceGatewayProvider(DeviceRegistry registry,
                                            DeviceSessionManager sessionManager,
                                            ProtocolSupports protocolSupports,
                                            NetworkManager networkManager) {
        this.registry = registry;
        this.sessionManager = sessionManager;
        this.protocolSupports = protocolSupports;
        this.networkManager = networkManager;
    }

    @Override public String getId() { return "mqtt-server"; }
    @Override public String getName() { return "MQTT Server 网关"; }
    @Override public Transport getTransport() { return DefaultTransport.MQTT; }

    @Override
    public Mono<DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties) {
        return networkManager.<MqttServer>getNetwork(getNetworkType(), properties.getComponentId())
                .map(network -> new MqttServerDeviceGateway(
                        properties.getId(), registry, sessionManager,
                        network, messageHandler, Mono.empty()));
    }
    public Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties) {
        return Mono.just(new MqttServerDeviceGateway(properties, registry, sessionManager, protocolSupports));
    }
}
```
**关键技术点：** `createDeviceGateway` 通过 `NetworkManager.getNetwork(type, componentId)` 获取底层网络通道，再包装为 DeviceGateway 实例。DeviceGateway 不自己创建网络连接，而是复用 Network 组件的能力。

所有 `DeviceGatewayProvider` 实现类统一构造函数依赖：

| 依赖 | 用途 |
|------|------|
| `DeviceRegistry` | 设备查找与认证 |
| `DeviceSessionManager` | 设备连接时创建/管理会话 |
| `ProtocolSupports` | 协议编解码与认证代理 |
| `NetworkManager` | 获取底层网络组件（Network） |

### 5.5 DeviceGateway 实现类

| 实现类 | 对应 Provider | 持有的网络资源 |
|--------|-------------|--------------|
| `TcpDeviceGateway` | TcpDeviceGatewayProvider | Netty ServerSocketChannel |
| `HttpServerDeviceGateway` | HttpServerDeviceGatewayProvider | Netty HTTP Channel |
| `MqttServerDeviceGateway` | MqttServerDeviceGatewayProvider | 内嵌 MQTT Broker 实例 |
| `MqttClientDeviceGateway` | MqttClientDeviceGatewayProvider | MQTT Client 连接 |
| `CoapDeviceGateway` | CoapDeviceGatewayProvider | CoAP Server Endpoint |
| `WebSocketDeviceGateway` | WebSocketDeviceGatewayProvider | WebSocket 连接 |
| `ChildrenDeviceGateway` | ChildrenDeviceGatewayProvider | 父网关引用（无独立网络资源） |

每个实现类通过 `onMessage()` 将协议原始消息转换为 `Flux<Message>`，`shutdown()` 关闭对应的网络资源。

### 5.6 通用抽象基类

```java
public abstract class AbstractDeviceGateway implements DeviceGateway {
    protected final DeviceGatewayProperties properties;
    protected volatile GatewayState state = GatewayState.shutdown;

    @Override public String getId() { return properties.getId(); }
    @Override public GatewayState getState() { return state; }
    // startup/pause/shutdown 由子类实现
}
```

### 5.7 GatewayPropertiesManagerImpl（sa-admin 模块）

位于 `net.lab1024.sa.admin.module.business.gateway.service`，从 `gateway` 表读取配置：

```java
@Service
public class GatewayPropertiesManagerImpl implements DeviceGatewayPropertiesManager {

    @Resource
    private GatewayDao gatewayDao;

    @Override
    public Mono<DeviceGatewayProperties> getProperties(String id) {
        GatewayEntity entity = gatewayDao.selectById(Long.valueOf(id));
        return entity != null ? Mono.just(entity.toProperties()) : Mono.empty();
    }

    @Override
    public Flux<DeviceGatewayProperties> getAllProperties() {
        List<GatewayEntity> list = gatewayDao.selectList(null);
        return Flux.fromIterable(list).map(GatewayEntity::toProperties);
    }
}
```

转换逻辑内聚在 `GatewayEntity.toProperties()` 中：

```java
public DeviceGatewayProperties toProperties() {
    DeviceGatewayProperties p = new DeviceGatewayProperties();
    p.setId(String.valueOf(id));
    p.setName(name);
    p.setDescription(description);
    p.setProvider(type);                    // type → provider
    p.setProtocol(String.valueOf(protocolId));
    p.setTransport(transport);
    p.setStatus(status);
    return p;
}
```

**映射关系：**

| GatewayEntity 字段 | → | DeviceGatewayProperties 字段 |
|-------------------|---|-----------------------------|
| `id` | → | `id` (String.valueOf) |
| `name` | → | `name` |
| `description` | → | `description` |
| `type` | → | `provider` |
| `protocolId` | → | `protocol` |
| `transport` | → | `transport` |
| `status` | → | `status` |
```

**映射关系：**

| GatewayEntity 字段 | → | DeviceGatewayProperties 字段 |
|-------------------|---|-----------------------------|
| `id` | → | `id` (String.valueOf) |
| `name` | → | `name` |
| `description` | → | `description` |
| `type` | → | `provider` |
| `protocolId` | → | `protocol` |
| `transport` | → | `transport` |
| `status` | → | `status` |

## 六、自动装配

```java
@Configuration
public class DeviceGatewayAutoConfiguration {

    @Bean(initMethod = "init")
    public DeviceGatewayManager deviceGatewayManager(DeviceGatewayProviders providers,
                                                      DeviceGatewayPropertiesManager propertiesManager,
                                                      IEventBus eventBus) {
        return new DefaultDeviceGatewayManager(providers, propertiesManager, eventBus);
    }

    @Bean
    public DeviceGatewayProviders deviceGatewayProviders(List<DeviceGatewayProvider> providerList) {
        return new DefaultDeviceGatewayProviders(providerList);
    }

    @Bean
    public DeviceGatewayProvider childrenDeviceGatewayProvider(DeviceRegistry registry,
                                                                DeviceSessionManager sessionManager,
                                                                ProtocolSupports protocolSupports) {
        return new ChildrenDeviceGatewayProvider(registry, sessionManager, protocolSupports);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        event.getApplicationContext()
                .getBean(DefaultDeviceGatewayManager.class)
                .loadAll()
                .subscribe();
    }
}
```

各个 `DeviceGatewayProvider` 的 Bean 定义分散在各自协议的 config 中：

```java
// net.lab1024.sa.base.module.support.protocol.tcp.config.TcpProtocolConfig
@Configuration
public class TcpProtocolConfig {
    @Bean
    public DeviceGatewayProvider tcpDeviceGatewayProvider(DeviceRegistry registry,
                                                           DeviceSessionManager sessionManager,
                                                           ProtocolSupports protocolSupports) {
        return new TcpDeviceGatewayProvider(registry, sessionManager, protocolSupports);
    }
}

// net.lab1024.sa.base.module.support.protocol.http.config.HttpProtocolConfig
@Configuration
public class HttpProtocolConfig {
    @Bean
    public DeviceGatewayProvider httpServerDeviceGatewayProvider(DeviceRegistry registry,
                                                                  DeviceSessionManager sessionManager,
                                                                  ProtocolSupports protocolSupports) {
        return new HttpServerDeviceGatewayProvider(registry, sessionManager, protocolSupports);
    }
}

// ... 其他协议同理（mqtt/coap/websocket）
```

`DefaultDeviceGatewayProviders` 通过 `List<DeviceGatewayProvider>` 自动收集所有协议配置中定义的 Provider Bean。

## 七、配置 POJO


```java
@Getter @Setter
public class DeviceGatewayProperties {
    private String id;           // 网关ID
    private String name;         // 网关名称
    private String description;  // 描述
    private String provider;     // 对应的 DeviceGatewayProvider.getId()
    private String protocol;     // 协议（如消息编解码协议）
    private String transport;    // 传输层
    private Integer status;      // 1=启用, 0=禁用
}
```

`provider` 字段决定由哪个 `DeviceGatewayProvider` 来创建该网关实例。

## 八、生命周期状态机

```
  [初始]
    │
    ▼
starting ──► started ──► paused ──► shutdown
                     │                    ▲
                     └────────────────────┘
                          (可直接关闭)
```

| 状态 | 含义 |
|------|------|
| `starting` | 启动中 |
| `started` | 已启动，正常收发消息 |
| `paused` | 已暂停，停止处理消息但未释放资源 |
| `shutdown` | 已关闭，资源已释放 |

## 九、典型启动流程

```
1. DeviceGatewayPropertiesManager.getAllProperties()
       → Flux<DeviceGatewayProperties>    // 获取所有网关配置
2. 遍历每条配置:
   a. DeviceGatewayProviders.getProvider(props.getProvider())
         → DeviceGatewayProvider
   b. provider.createDeviceGateway(props)
         → DeviceGateway
   c. gateway.startup()
   d. gateway.onMessage().subscribe(...)   // 订阅设备消息
   e. DeviceGatewayManager 注册网关实例
```

## 十、与 ProtocolSupport 的关系

`DeviceGateway` 关注的是**接入层的网络通道**（TCP Server、MQTT Broker Client、HTTP Endpoint），`ProtocolSupport` 关注的是**协议层的编解码和认证**。

二者的连接点在 `DeviceGatewayProvider.createDeviceGateway()` 内部：
- 创建网络连接（如 Netty TCP Server）
- 根据 `properties.protocol` 查找对应的 `ProtocolSupport`
- 将协议的 `DeviceMessageCodec` 注入网关的消息处理管道
- 认证委托给 `ProtocolSupport.authenticate()`

```
DeviceGateway（接入层）          ProtocolSupport（协议层）
     │                               │
     ├─ 建立连接                      │
     ├─ 收原始数据                     │
     ├───────────→ decode() ─────────→│  编解码
     ├───────────→ authenticate() ───→│  认证
     ├─ 发布 Message                   │
     │                               │
```

## 十一、验证清单

1. 实现 `DeviceGatewayProvider` 并注册 → `getProvider(id)` 可获取
2. 调用 `createDeviceGateway(properties)` → 返回网关实例
3. 调用 `startup()` → 状态变为 `started`，开始接收消息
4. 订阅 `onMessage()` → 收到设备消息
5. 调用 `pause()` → 状态变为 `paused`，暂停消息处理
6. 调用 `shutdown()` → 状态变为 `shutdown`，资源释放
7. `doOnShutdown(disposable)` → 关闭时自动 dispose
8. 新增网关（status=1）→ `GatewayEventHandler` 发布 `DeviceGatewayEvent.SAVED` → `DefaultDeviceGatewayManager` 创建并启动网关
9. 修改网关（status 1→0）→ 发布 `DELETED` → 关闭并移除网关
10. 修改网关（status 0→1）→ 发布 `UPDATED` → 重载并启动网关
11. 删除网关 → 发布 `DELETED` → 关闭网关
12. 应用启动 → `ApplicationReadyEvent` → `loadAll()` → 加载所有已启用网关
