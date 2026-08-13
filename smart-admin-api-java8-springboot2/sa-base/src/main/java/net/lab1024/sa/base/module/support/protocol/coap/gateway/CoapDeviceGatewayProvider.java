package net.lab1024.sa.base.module.support.protocol.coap.gateway;

import net.lab1024.sa.base.common.gateway.DeviceGateway;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProperties;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;
import net.lab1024.sa.base.common.message.codec.Transport;
import reactor.core.publisher.Mono;

public class CoapDeviceGatewayProvider implements DeviceGatewayProvider {

    @Override public String getId() { return "coap"; }
    @Override public String getName() { return "CoAP 网关"; }
    @Override public Transport getTransport() { return DefaultTransport.CoAP; }

    @Override
    public Mono<? extends DeviceGateway> createDeviceGateway(DeviceGatewayProperties properties) {
        return Mono.just(new CoapDeviceGateway(properties));
    }
}
