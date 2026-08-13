package net.lab1024.sa.base.module.support.protocol.children.gateway;

import net.lab1024.sa.base.common.gateway.DeviceGateway;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.common.message.codec.Transport;
import reactor.core.publisher.Mono;

/**
 * 子设备网关 Provider — 不持有网络连接，通过父网关代理通信。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
public class ChildrenDeviceGatewayProvider implements DeviceGatewayProvider {

    @Override public String getId() { return "children"; }
    @Override public String getName() { return "子设备网关"; }
    @Override public Transport getTransport() { return null; }

    @Override
    public Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties) {
        return Mono.just(new ChildrenDeviceGateway(properties));
    }
}
