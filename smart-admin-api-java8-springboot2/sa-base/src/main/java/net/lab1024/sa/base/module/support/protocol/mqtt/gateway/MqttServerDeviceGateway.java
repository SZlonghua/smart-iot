package net.lab1024.sa.base.module.support.protocol.mqtt.gateway;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.message.DecodedClientMessageHandler;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.common.gateway.AbstractDeviceGateway;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttServerNetwork;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Getter
@Slf4j
public class MqttServerDeviceGateway extends AbstractDeviceGateway {

    //设备注册中心
    private final DeviceRegistry registry;

    //设备会话管理器
    private final DeviceSessionManager sessionManager;

    //Mqtt 服务
    @Setter
    private MqttServerNetwork mqttServerNetwork;

    //解码后的设备消息处理器
    private final DecodedClientMessageHandler messageHandler;
    @Setter
    private Mono<ProtocolSupport> protocolSupport;

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
        return super.onMessage();
    }

    @Override
    protected Mono<Void> doStartup() {
        log.info("[MqttServerGateway] 启动 — id={}", getId());
        return Mono.empty();
    }

    @Override
    protected Mono<Void> doShutdown() {
        log.info("[MqttServerGateway] 关闭 — id={}", getId());
        return Mono.empty();
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
}
