package net.lab1024.sa.base.module.support.protocol.children.gateway;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.DeviceGateway;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.GatewayState;
import net.lab1024.sa.base.common.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 子设备网关 — 不持有网络连接，通过父网关代理通信。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
@Slf4j
public class ChildrenDeviceGateway implements DeviceGateway {

    private final DeviceGatewayProperties properties;
    private volatile GatewayState state = GatewayState.shutdown;

    public ChildrenDeviceGateway(DeviceGatewayProperties properties) {
        this.properties = properties;
    }

    @Override public String getId() { return properties.getId(); }
    @Override public GatewayState getState() { return state; }
    @Override public Flux<Message> onMessage() { return Flux.empty(); }

    @Override public Mono<Void> startup() {
        state = GatewayState.started;
        log.info("[ChildrenGateway] 注册 — id={}", properties.getId());
        return Mono.empty();
    }

    @Override public Mono<Void> pause() {
        state = GatewayState.paused;
        return Mono.empty();
    }

    @Override public Mono<Void> shutdown() {
        state = GatewayState.shutdown;
        log.info("[ChildrenGateway] 关闭 — id={}", properties.getId());
        return Mono.empty();
    }
}
