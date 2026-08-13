package net.lab1024.sa.base.module.support.protocol.websocket.gateway;

import net.lab1024.sa.base.common.gateway.DeviceGateway;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;
import net.lab1024.sa.base.common.message.codec.Transport;
import reactor.core.publisher.Mono;

public class WebSocketDeviceGatewayProvider implements DeviceGatewayProvider {

    @Override public String getId() { return "websocket"; }
    @Override public String getName() { return "WebSocket 网关"; }
    @Override public Transport getTransport() { return DefaultTransport.WebSocket; }

    @Override
    public Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties) {
        return Mono.just(new WebSocketDeviceGateway(properties));
    }
}
