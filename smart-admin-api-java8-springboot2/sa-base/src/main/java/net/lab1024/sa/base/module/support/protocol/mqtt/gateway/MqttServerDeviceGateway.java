package net.lab1024.sa.base.module.support.protocol.mqtt.gateway;

import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.AbstractDeviceGateway;
import net.lab1024.sa.base.common.gateway.GatewayState;
import net.lab1024.sa.base.common.message.DecodedClientMessageHandler;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;
import net.lab1024.sa.base.common.message.codec.Transport;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.device.AuthenticationRequest;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttServerNetwork;
import net.lab1024.sa.base.module.support.protocol.mqtt.session.MqttConnectionSession;
import net.lab1024.sa.base.module.support.protocol.mqtt.session.VertxMqttConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;


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

    @Setter
    private Mono<ProtocolSupport> protocolSupport;

    private final Sinks.Many<Message> gatewayMessageSink = Sinks.many().multicast().onBackpressureBuffer();

    public MqttServerDeviceGateway(String id,
                                   DeviceRegistry registry,
                                   DeviceSessionManager sessionManager,
                                   MqttServerNetwork mqttServerNetwork,
                                   DecodedClientMessageHandler messageHandler,
                                   Mono<ProtocolSupport> protocolSupport) {
        super(id);
        this.registry = registry;
        this.sessionManager = sessionManager;
        this.mqttServerNetwork = mqttServerNetwork;
        this.messageHandler = messageHandler;
        this.protocolSupport = protocolSupport;
    }

    @Override
    public Flux<Message> onMessage() {
        return gatewayMessageSink.asFlux();
    }

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
                    AuthenticationRequest authReq = connection.getAuthenticationRequest();
                    return protocolSupport
                            .flatMap(ps -> ps.authenticate(authReq, registry))
                            .flatMap(authResp -> {
                                if (!authResp.isSuccess()) {
                                    connection.reject(MqttConnectReturnCode.CONNECTION_REFUSED_NOT_AUTHORIZED);
                                    return Mono.empty();
                                }
                                // 认证成功，设置设备 ID clientId不一定是设备ID
                                connection.setDeviceId(authResp.getDeviceId());
                                return Mono.just(connection);
                            });
                })
                // 3. 会话管理 — compute 替换
                .flatMap(connection -> {
                    return registry.getDevice(connection.getDeviceId())
                            .flatMap(deviceOperator -> Mono.just(new MqttConnectionSession(connection, deviceOperator, getTransport())))
                            .doOnNext(session -> sessionManager.compute(session.getDeviceId(),
                                    oldSessionMono -> oldSessionMono
                                            .cast(MqttConnectionSession.class)
                                            .doOnNext(MqttConnectionSession::close)
                                            .thenReturn(session)
                            ))
                            .thenReturn(connection);
                    /*return sessionManager.compute(session.getDeviceId(),
                                    oldSession -> Mono.just(session))
                            .thenReturn(connection);*/
                })
                // 4. accept — 回复 CONNACK
                .doOnNext(VertxMqttConnection::accept)
                // 5. init — 注册所有 handler
                .doOnNext(VertxMqttConnection::init)
                // 6-7. 消息处理链 — 编解码 + 路由
                .flatMap(connection ->
                        connection.receiveMessage()
                                .flatMap(encodedMsg ->
                                        protocolSupport
                                                .flatMap(ps -> ps.getMessageCodec(getTransport()))
                                                .flatMap(codec -> Mono.from(codec.decode(null)))
                                )
                                .flatMap(message ->
                                        // 如果是子设备消息 并且是上线消息 请进行子设备认证（预留） 认证成功注册子设备会话 失败发送子设备认证失败消息


                                        messageHandler.handle(message)
                                                .doOnSuccess(v -> gatewayMessageSink.tryEmitNext(message))
                                )
                                .doOnError(err -> {
                                    log.error("消息处理异常", err);
                                    connection.disconnect();
                                })
                                .then()
                )
                // 断连清理
                .doOnTerminate(() -> {
                    /* 清理会话, 发布 DeviceOfflineMessage */
                })
                .subscribe();
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
