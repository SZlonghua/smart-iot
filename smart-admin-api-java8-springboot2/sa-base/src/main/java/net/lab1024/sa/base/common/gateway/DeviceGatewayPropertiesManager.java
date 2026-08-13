package net.lab1024.sa.base.common.gateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceGatewayPropertiesManager {

    Mono<DeviceGatewayProperties> getProperties(String id);

    Flux<DeviceGatewayProperties> getAllProperties();
}
