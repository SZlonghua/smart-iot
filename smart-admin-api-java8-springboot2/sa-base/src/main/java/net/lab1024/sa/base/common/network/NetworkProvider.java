package net.lab1024.sa.base.common.network;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

public interface NetworkProvider<C extends NetworkConfig> {

    @Nonnull
    String getId();

    @Nonnull
    NetworkType getType();

    @Nonnull
    Mono<C> createConfig(@Nonnull NetworkProperties properties);

    @Nonnull
    Mono<Network> createNetwork(@Nonnull C config);

    Mono<Network> reload(@Nonnull Network network, @Nonnull C config);
}
