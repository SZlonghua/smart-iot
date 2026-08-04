package net.lab1024.sa.base.common.network;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NetworkProviders<C extends NetworkConfig> {

    Mono<NetworkProvider<C>> getProvider(String provider);

    Flux<NetworkProvider<C>> getProviders();

    Mono<Void> addNetworkProvider(NetworkProvider<C> provider);
}
