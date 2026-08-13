package net.lab1024.sa.base.module.support.protocol.http.gateway;

import net.lab1024.sa.base.common.gateway.DeviceGateway;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;
import net.lab1024.sa.base.common.message.codec.Transport;
import reactor.core.publisher.Mono;

public class HttpServerDeviceGatewayProvider implements DeviceGatewayProvider {

    @Override public String getId() { return "http-server"; }
    @Override public String getName() { return "HTTP Server 网关"; }
    @Override public Transport getTransport() { return DefaultTransport.HTTP; }

    @Override
    public Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties) {
        return Mono.just(new HttpServerDeviceGateway(properties));
    }
}
