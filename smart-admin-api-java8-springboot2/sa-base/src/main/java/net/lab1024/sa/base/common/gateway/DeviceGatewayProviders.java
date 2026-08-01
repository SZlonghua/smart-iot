package net.lab1024.sa.base.common.gateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface DeviceGatewayProviders {

    Mono<DeviceGatewayProvider> getProvider(String provider);

    Flux<DeviceGatewayProvider> getProviders();

    Mono<Void> addGatewayProvider(DeviceGatewayProvider provider);
}
