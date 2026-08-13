package net.lab1024.sa.base.module.support.protocol.tcp.gateway;

import net.lab1024.sa.base.common.gateway.DeviceGateway;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;
import net.lab1024.sa.base.common.message.codec.Transport;
import reactor.core.publisher.Mono;

public class TcpDeviceGatewayProvider implements DeviceGatewayProvider {

    @Override public String getId() { return "tcp"; }
    @Override public String getName() { return "TCP 网关"; }
    @Override public Transport getTransport() { return DefaultTransport.TCP; }

    @Override
    public Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties) {
        return Mono.just(new TcpDeviceGateway(properties));
    }
}
