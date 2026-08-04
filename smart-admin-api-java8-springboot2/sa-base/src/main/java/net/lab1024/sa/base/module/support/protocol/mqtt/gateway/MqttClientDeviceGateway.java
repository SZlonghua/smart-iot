package net.lab1024.sa.base.module.support.protocol.mqtt.gateway;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.common.gateway.AbstractDeviceGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class MqttClientDeviceGateway extends AbstractDeviceGateway {

    public MqttClientDeviceGateway(DeviceGatewayProperties properties) { super(properties.getId()); }

    @Override protected Mono<Void> doStartup() {
        log.info("[MqttClientGateway] 启动 — id={}", getId()); return Mono.empty();
    }
    @Override protected Mono<Void> doShutdown() {
        log.info("[MqttClientGateway] 关闭 — id={}", getId()); return Mono.empty();
    }
}
