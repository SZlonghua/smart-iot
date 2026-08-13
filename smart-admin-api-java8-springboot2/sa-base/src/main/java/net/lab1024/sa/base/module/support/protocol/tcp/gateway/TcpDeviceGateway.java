package net.lab1024.sa.base.module.support.protocol.tcp.gateway;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.message.Message;
import net.lab1024.sa.base.common.gateway.AbstractDeviceGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * TCP 设备网关。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
@Slf4j
public class TcpDeviceGateway extends AbstractDeviceGateway {

    public TcpDeviceGateway(DeviceGatewayProperties properties) {
        super(properties.getId());
    }

    @Override
    protected Mono<Void> doStartup() {
        log.info("[TcpGateway] 启动 — id={}", getId());
        // TODO: 绑定 Netty TCP Server
        return Mono.empty();
    }

    @Override
    protected Mono<Void> doShutdown() {
        log.info("[TcpGateway] 关闭 — id={}", getId());
        return Mono.empty();
    }
}
