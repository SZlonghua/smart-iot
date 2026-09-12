package net.lab1024.sa.base.module.support.protocol.mqtt.gateway;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.cluster.ClusterManager;
import net.lab1024.sa.base.common.gateway.AbstractDeviceGateway;
import net.lab1024.sa.base.common.gateway.GatewayState;
import net.lab1024.sa.base.common.message.AbstractDeviceMessage;
import net.lab1024.sa.base.common.message.ChildDeviceMessage;
import net.lab1024.sa.base.common.message.ChildDeviceMessageReply;
import net.lab1024.sa.base.common.message.DecodedClientMessageHandler;
import net.lab1024.sa.base.common.message.DeviceOfflineMessage;
import net.lab1024.sa.base.common.message.DeviceOnlineMessage;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;
import net.lab1024.sa.base.common.message.codec.FromDeviceMessageContext;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.common.message.raw.EncodedMessage;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.device.AuthenticationRequest;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.device.session.support.ChildDeviceSession;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import net.lab1024.sa.base.module.support.protocol.mqtt.message.MqttEncodedMessage;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttServerNetwork;
import net.lab1024.sa.base.module.support.protocol.mqtt.session.MqttConnectionSession;
import net.lab1024.sa.base.module.support.protocol.mqtt.session.VertxMqttConnection;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Function;


/**
 * MQTT Server 设备网关。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/07
 * &#064;Copyright  1024创新实验室
 */
@Getter
@Slf4j
public class MqttServerDeviceGateway extends AbstractDeviceGateway {

    private final DeviceRegistry registry;
    private final DeviceSessionManager sessionManager;

    @Setter
    private MqttServerNetwork mqttServerNetwork;

    private final DecodedClientMessageHandler messageHandler;

    /** 事件总线 — 连接建立/断开型上/下线消息发布（无设备消息，需构造补齐） */
    private final IEventBus eventBus;

    @Setter
    private Mono<ProtocolSupport> protocolSupport;

    /** 集群管理器 — 取当前节点 ID（会话注册时写入 connectionServerId）；单机部署（未装配集群）为 null */
    @Nullable
    private final ClusterManager clusterManager;

    private final Sinks.Many<Message> gatewayMessageSink = Sinks.many().multicast().onBackpressureBuffer();

    public MqttServerDeviceGateway(String id,
                                   DeviceRegistry registry,
                                   DeviceSessionManager sessionManager,
                                   MqttServerNetwork mqttServerNetwork,
                                   DecodedClientMessageHandler messageHandler,
                                   IEventBus eventBus,
                                   Mono<ProtocolSupport> protocolSupport,
                                   @Nullable ClusterManager clusterManager) {
        super(id);
        this.registry = registry;
        this.sessionManager = sessionManager;
        this.mqttServerNetwork = mqttServerNetwork;
        this.messageHandler = messageHandler;
        this.eventBus = eventBus;
        this.protocolSupport = protocolSupport;
        this.clusterManager = clusterManager;
    }

    /** 当前节点 ID（集群节点 ID）— 集群未启用（未注入集群管理器）返回 null，会话注册时写入 connectionServerId */
    @Nullable
    private String getCurrentNodeId() {
        return clusterManager == null ? null : clusterManager.getCurrentNodeId();
    }

    /*@Override
    public Flux<Message> onMessage() {
        return gatewayMessageSink.asFlux();
    }*/

    @Override
    protected Mono<Void> doStartup() {
        return Mono.just(mqttServerNetwork)
                .doOnNext(network -> network.onConnection(this::handleConnection))
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
                .filter(connection -> getState() == GatewayState.started)
                .switchIfEmpty(Mono.fromRunnable(() ->
                        conn.reject(MqttConnectReturnCode.CONNECTION_REFUSED_SERVER_UNAVAILABLE))
                        .then(Mono.empty()))
                // 2. 设备认证
                .flatMap(connection -> {
                    AuthenticationRequest authReq = connection.getAuthenticationRequest(getTransport());
                    return protocolSupport
                            .flatMap(ps -> ps.authenticate(authReq, registry))
                            .flatMap(authResp -> {
                                if (!authResp.isSuccess()) {
                                    connection.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED);
                                    return Mono.empty();
                                }
                                // 认证成功，回填设备标识（deviceId 数据库ID，productKey/deviceKey 烧录标识）
                                connection.setDeviceId(authResp.getDeviceId());
                                connection.setProductKey(authReq.getProductKey());
                                connection.setDeviceKey(authReq.getDeviceKey());
                                return Mono.just(connection);
                            });
                })
                // 3. 会话管理 — compute 替换（替换/关闭旧会话由 SessionManager 内部处理）
                //    注意：compute 返回的 Mono 必须被订阅（flatMap），否则注册逻辑不执行
                //    会话携带协议实例 ID + 当前节点 ID，注册事件写入 Redis 设备字段（DefaultDecodedClientMessageHandler）
                //    连接建立无设备消息 → 传入注册成功回调（this::publishOnline）构造上线消息发布；
                //    连接断开同理，回调随会话携带（this::publishOffline）在注销成功时发布下线消息
                .flatMap(connection ->
                        protocolSupport
                                .flatMap(ps -> registry.getDevice(connection.getDeviceId())
                                        .map(deviceOperator -> new MqttConnectionSession(connection, deviceOperator,
                                                getTransport(), sessionManager, getId(),
                                                ps.getId(), getCurrentNodeId(), this::publishOffline)))
                                .flatMap(session -> sessionManager.compute(session.getDeviceId(),
                                        oldSessionMono -> Mono.just(session), this::publishOnline)
                                        .thenReturn(connection))
                )
                // 4. accept — 回复 CONNACK
                .doOnNext(VertxMqttConnection::accept)
                // 5. init — 注册所有 handler
                .doOnNext(VertxMqttConnection::init)
                // 6. 消息处理链 — 编解码 + 路由
                .flatMap(connection ->
                        connection.receiveMessage()
                                .flatMap(encodedMsg -> {
                                    log.info("received encoded message: {}", encodedMsg);
                                    // 收到消息后判定网关状态 — 非 started 丢弃消息
                                    if (getState() != GatewayState.started) {
                                        // 丢弃消息 如暂停状态下不接受任何消息和连接
                                        log.warn("[MQTT] 网关状态非 started, 丢弃消息: state={}, clientId={}", getState(), connection.getClientId());
                                        return Mono.empty();
                                    }
                                    return handleMessage(connection, encodedMsg);
                                })
                                .doOnError(err -> {
                                    log.error("消息处理异常", err);
                                    connection.disconnect();
                                })
                                .then()
                )
                .subscribe();
    }

    /** 处理设备上行消息 — 编解码 → 会话处理 → 回填 deviceId → 消息路由 */
    private Mono<Void> handleMessage(VertxMqttConnection connection, EncodedMessage encodedMsg) {
        return protocolSupport
                .flatMap(ps -> ps.getMessageCodec(getTransport()))
                .flatMap(codec -> sessionManager.getSession(connection.getDeviceId())
                        .map(session -> FromDeviceMessageContext.of(session, encodedMsg, registry, connection))
                        .flatMap(context -> Mono.from(codec.decode(context)))
                )
                .flatMap(message -> {
                    // 解码成功 → 延迟 ACK（QoS1 PUBACK / QoS2 PUBREC）；解码失败不 ack → doOnError 断开连接
                    if (encodedMsg instanceof MqttEncodedMessage) {
                        ((MqttEncodedMessage) encodedMsg).acknowledge();
                    }
                    return handleDeviceSession(connection, message);
                })
                .flatMap(message -> fillDeviceId(connection, message))
                // 过滤 deviceId 为空的消息（未回填到设备 ID 的消息无法路由，直接丢弃）要么会话不存在 要么设备不存在
                // 注意：子设备消息外层 deviceId 是网关 ID（恒非空），必须检查内层 deviceId
                .filter(this::hasDeviceId)
                .flatMap(message -> {
                    // 最后才处理真正的消息 — 设备上报的 online/offline 消息同样进入消息流
                    //（原 messageId/时间戳/headers 保留落库）；连接建立/断开型变更无设备消息，
                    // 由网关注册/注销回调构造发布 — 两来源互斥，不重复
                    return messageHandler.handle(message)
                            .doOnSuccess(v -> log.info("message handled: {}", message));
                });
    }

    /**
     * 校验消息 deviceId 是否已回填 — 未回填说明会话/设备不存在，无法路由，直接丢弃。
     * 丢弃日志统一在这里输出（回填阶段不报错，避免日志分散两处）。
     * 注意：子设备消息外层 deviceId 是网关 ID（恒非空），必须解包检查内层。
     */
    private boolean hasDeviceId(Message message) {
        Message target = unwrapChild(message);
        // 解码链路产出的消息均为 AbstractDeviceMessage（子设备消息解包后亦然），无需再判类型
        boolean has = StringUtils.isNotEmpty(((AbstractDeviceMessage) target).getDeviceId());
        if (!has) {
            log.error("[MQTT] 丢弃消息 — deviceId 未回填（子设备未上线或设备不存在）: {}", message);
        }
        return has;
    }

    /** 子设备消息解包 — 返回内层消息，非子设备消息返回自身（值传递，不覆盖原引用） */
    private static Message unwrapChild(Message message) {
        if (message instanceof ChildDeviceMessage) {
            return ((ChildDeviceMessage<?>) message).getChildDeviceMessage();
        }
        if (message instanceof ChildDeviceMessageReply) {
            return ((ChildDeviceMessageReply<?>) message).getChildDeviceMessage();
        }
        return message;
    }

    /**
     * 回填数据库设备 ID（topic 中只有烧录的 key）：
     * - 直连消息：取当前连接 deviceId（认证时已设置）
     * - 子设备消息：内层消息通过子设备会话缓存查找 deviceId，不查 registry
     */
    private Mono<Message> fillDeviceId(VertxMqttConnection connection, Message message) {
        // 子设备消息 → 外层回填网关 deviceId，内层按 (childProductKey, childDeviceKey) 查子设备会话
        if (message instanceof ChildDeviceMessage) {
            ChildDeviceMessage<?> child = (ChildDeviceMessage<?>) message;
            child.setDeviceId(connection.getDeviceId());
            return fillChildDeviceId(child.getChildProductKey(), child.getChildDeviceKey(), child.getChildDeviceMessage())
                    .thenReturn(message);
        }
        if (message instanceof ChildDeviceMessageReply) {
            ChildDeviceMessageReply<?> child = (ChildDeviceMessageReply<?>) message;
            child.setDeviceId(connection.getDeviceId());
            return fillChildDeviceId(child.getChildProductKey(), child.getChildDeviceKey(), child.getChildDeviceMessage())
                    .thenReturn(message);
        }
        // 直连消息 → 当前连接 deviceId
        if (message instanceof AbstractDeviceMessage) {
            ((AbstractDeviceMessage) message).setDeviceId(connection.getDeviceId());
        }
        return Mono.just(message);
    }

    /**
     * 子设备内层消息回填 deviceId — 通过子设备会话缓存查找。
     * 会话不存在则静默不填（丢弃日志统一在 hasDeviceId 过滤时输出，避免分散）。
     * 注意：这里不要用 registry.getDevice(childProductKey, childDeviceKey)获取deviceId
     * 高并发下会产生 DeviceOperator 大量创建与销毁，频繁 GC 内存爆炸。
     */
    private Mono<Void> fillChildDeviceId(String childProductKey, String childDeviceKey, Message inner) {
        if (inner instanceof AbstractDeviceMessage) {
            return sessionManager.getSession(childProductKey, childDeviceKey)
                    .doOnNext(session -> ((AbstractDeviceMessage) inner).setDeviceId(session.getDeviceId()))
                    .then();
        }
        return Mono.empty();
    }

    /**
     * 处理设备会话：
     * - 子设备消息 → 处理子设备会话（上线注册 / 下线注销 / 普通消息刷新）
     * - 直连设备消息 → 会话在连接建立时已注册（重复注册不影响），处理下线注销
     * <p>
     * 消息驱动的会话变更不传注册/注销回调 — 设备上报的 online/offline 原消息本身进入消息流
     *（原 messageId/时间戳/headers 保留），无需构造发布，避免重复；
     * 连接建立/断开型变更（无设备消息）在网关侧传入回调（this::publishOnline / this::publishOffline）补齐。
     */
    private Mono<Message> handleDeviceSession(VertxMqttConnection connection, Message message) {
        log.info("handleDeviceSession decoded message: {}", message);
        // 子设备认证默认是网关能接入就是正常设备 这里没有认证 如需要请增加一个认证消息类型并在这里认证 另外直连设备（网关）只支持一机一密认证
        // 子设备认证有多种方式 如：增加认证消息类型 增加预注册消息类型用产品密钥换设备密钥（加密）或token 还有在app端扫描(wifi 蓝牙)设备添加时直接返回设备密钥（加密）等等 请根据实际情况来

        if (message instanceof ChildDeviceMessage) {
            ChildDeviceMessage<?> child = (ChildDeviceMessage<?>) message;
            return handleChildDeviceSession(connection, child.getChildProductKey(), child.getChildDeviceKey(), child.getChildDeviceMessage())
                    .thenReturn(message);
        }
        if (message instanceof ChildDeviceMessageReply) {
            ChildDeviceMessageReply<?> child = (ChildDeviceMessageReply<?>) message;
            return handleChildDeviceSession(connection, child.getChildProductKey(), child.getChildDeviceKey(), child.getChildDeviceMessage())
                    .thenReturn(message);
        }
        // 直连设备上线 → 幂等注册会话（连接时已注册，重复注册不影响当前连接）
        if (message instanceof DeviceOnlineMessage) {
            return handleDirectDeviceOnline(connection, (DeviceOnlineMessage) message).thenReturn(message);
        }
        // 直连设备下线 → 注销会话（deviceId 尚未填充，用连接认证时的 deviceId；消息驱动 — 原消息进入消息流，不传注销回调）
        if (message instanceof DeviceOfflineMessage) {
            return sessionManager.remove(connection.getDeviceId())
                    .thenReturn(message);
        }
        return Mono.just(message);
    }

    /**
     * 处理直连设备上线 — 会话在连接建立时已注册，这里幂等注册：
     * 会话已存在则保持原会话（不能替换，否则会关闭当前连接），不存在则注册。
     * 消息驱动注册不传注册回调 — 设备原上线消息已进入消息流，不重复构造。
     */
    private Mono<Void> handleDirectDeviceOnline(VertxMqttConnection connection, DeviceOnlineMessage message) {
        // 此时 deviceId 尚未填充（fillDeviceId 在 handleDeviceSession 之后执行），按烧录的产品 key + 设备 key 查询
        return protocolSupport
                .flatMap(ps -> registry.getDevice(message.getProductKey(), message.getDeviceKey())
                        .flatMap(operator -> {
                            MqttConnectionSession session = new MqttConnectionSession(connection, operator,
                                    getTransport(), sessionManager, getId(), ps.getId(), getCurrentNodeId(),
                                    this::publishOffline);
                            return sessionManager.compute(session.getDeviceId(),
                                    old -> old.switchIfEmpty(Mono.just(session)));
                        }))
                .then();
    }

    /**
     * 处理子设备会话（消息驱动）：
     * - 仅上线/下线消息触发会话变更（上线注册 / 下线注销）
     * - 其他消息不处理会话，直接放行（避免每条消息查询 registry）
     */
    private Mono<Void> handleChildDeviceSession(VertxMqttConnection connection,
                                                String childProductKey, String childDeviceKey, Message inner) {
        // 非上线/下线消息不涉及会话变更，直接放行
        if (!(inner instanceof DeviceOnlineMessage) && !(inner instanceof DeviceOfflineMessage)) {
            return Mono.empty();
        }
        // 如果高并发下同一时刻有大量上线 离线消息请不要用此方法（基本很难遇到 一个子设备建立连接只发送一次） 这里偷懒了
        // 直接用cacheManager.getStringCache().get(KEY_PREFIX_MAPPING + productKey + "-" + deviceKey))获取deviceId
        // 并且整改所有AbstractDeviceSession接口 里面获取getOperator方法全部改为懒加载 构造函数传入（）->registry.getDevice(childProductKey, childDeviceKey)
        return registry.getDevice(childProductKey, childDeviceKey)
                .flatMap(operator -> sessionManager.getSession(connection.getDeviceId())
                        .flatMap(parent -> {
                            // 子设备下线 → 先回填内层 deviceId（会话移除后 fillChildDeviceId 查不到会话，
                            // 消息会因 deviceId 未回填被 hasDeviceId 丢弃）再注销子设备会话；消息驱动不传注销回调
                            if (inner instanceof DeviceOfflineMessage) {
                                ((AbstractDeviceMessage) inner).setDeviceId(operator.getDeviceId());
                                return sessionManager.remove(operator.getDeviceId()).then();
                            }
                            // 子设备上线 → 注册子设备会话（会话持有父网关会话引用，alive 跟随网关连接）；
                            // 旧会话仍存活则保留（重复上线幂等，避免频繁替换关闭会话），
                            // 旧会话失效或不存在（首次上线）→ 注册新会话；
                            // 消息驱动注册不传注册回调（设备原上线消息已进入消息流），注销回调随会话携带
                            //（父连接断开级联移除子会话时构造发布子设备下线消息）
                            ChildDeviceSession childSession = new ChildDeviceSession(
                                    operator.getDeviceId(), operator, getTransport(),
                                    childProductKey, childDeviceKey, parent, getId(), sessionManager,
                                    this::publishOffline);
                            return sessionManager.compute(childSession.getDeviceId(),
                                    old -> old.flatMap(existing -> existing.isAlive()
                                            ? Mono.just(existing)
                                            : Mono.just(childSession))
                                            .switchIfEmpty(Mono.just(childSession))).then();
                        }));
    }

    /**
     * 构造并发布上/下线消息 — 仅用于连接建立/断开型变更（无设备消息，需平台补齐）；
     * 设备上报的 online/offline 消息走消息流，不在此构造。
     * messageId 平台侧生成（同设备命令下发约定），时间戳/headers 信封由消息基类默认。
     */
    private void publishSessionMessage(DeviceSession session, AbstractDeviceMessage message) {
        message.setMessageId(UUID.randomUUID().toString());
        message.setDeviceId(session.getDeviceId());
        message.setProductKey(session.getProductKey());
        message.setDeviceKey(session.getDeviceKey());
        eventBus.publishAsync(message);
    }

    /** 会话注册成功（连接建立）→ 构造发布设备上线消息 */
    private void publishOnline(DeviceSession session) {
        publishSessionMessage(session, new DeviceOnlineMessage());
    }

    /** 会话注销成功（连接断开 / 父连接断开级联）→ 构造发布设备下线消息 */
    private void publishOffline(DeviceSession session) {
        publishSessionMessage(session, new DeviceOfflineMessage());
    }

    @Override
    protected Mono<Void> doShutdown() {
        return Mono.fromRunnable(() -> mqttServerNetwork.shutdown());
    }

    @Override
    public boolean isChangeNetwork(String networkId) {
        return getMqttServerNetwork().getId().equals(networkId);
    }

    @Override
    public boolean isChangeProtocol(String protocol) {
        return Boolean.TRUE.equals(getProtocolSupport()
                .map(ProtocolSupport::getId)
                .filter(id -> !id.equals(protocol))
                .hasElement()
                .block());
    }

    public Transport getTransport() {
        return DefaultTransport.MQTT;
    }
}
