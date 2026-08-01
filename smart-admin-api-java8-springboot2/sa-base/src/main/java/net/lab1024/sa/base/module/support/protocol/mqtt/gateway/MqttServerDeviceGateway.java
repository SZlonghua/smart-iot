package net.lab1024.sa.base.module.support.protocol.mqtt.gateway;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.module.support.protocol.gateway.AbstractDeviceGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class MqttServerDeviceGateway extends AbstractDeviceGateway {

    public MqttServerDeviceGateway(DeviceGatewayProperties properties) {
        super(properties);
    }

    @Override
    public Flux<Message> onMessage() {
        return super.onMessage();
    }

    @Override
    protected Mono<Void> doStartup() {
        log.info("[MqttServerGateway] 启动 — id={}", properties.getId());
        return Mono.empty();
    }

    @Override
    protected Mono<Void> doShutdown() {
        log.info("[MqttServerGateway] 关闭 — id={}", properties.getId());
        return Mono.empty();
    }
}
