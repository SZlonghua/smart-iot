package net.lab1024.sa.base.common.gateway;

import reactor.core.publisher.Mono;

public interface DeviceGatewayManager {

    Mono<DeviceGateway> getGateway(String id);

    Mono<Void> reload(String gatewayId);

    Mono<Void> start(String id);

    Mono<Void> shutdown(String gatewayId);

    void init();

    Mono<Void> loadAll();
}
