package net.lab1024.sa.base.module.support.protocol.http.gateway;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.module.support.protocol.gateway.AbstractDeviceGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class HttpServerDeviceGateway extends AbstractDeviceGateway {

    public HttpServerDeviceGateway(DeviceGatewayProperties properties) {
        super(properties);
    }

    @Override
    protected Mono<Void> doStartup() {
        log.info("[HttpGateway] 启动 — id={}", properties.getId());
        return Mono.empty();
    }

    @Override
    protected Mono<Void> doShutdown() {
        log.info("[HttpGateway] 关闭 — id={}", properties.getId());
        return Mono.empty();
    }
}
