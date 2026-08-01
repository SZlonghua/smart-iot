package net.lab1024.sa.base.module.support.protocol.websocket.gateway;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.module.support.protocol.gateway.AbstractDeviceGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class WebSocketDeviceGateway extends AbstractDeviceGateway {

    public WebSocketDeviceGateway(DeviceGatewayProperties properties) { super(properties); }

    @Override protected Mono<Void> doStartup() {
        log.info("[WebSocketGateway] 启动 — id={}", properties.getId()); return Mono.empty();
    }
    @Override protected Mono<Void> doShutdown() {
        log.info("[WebSocketGateway] 关闭 — id={}", properties.getId()); return Mono.empty();
    }
}
