package net.lab1024.sa.base.common.network;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface NetworkConfigManager {

    Mono<NetworkProperties> getConfig(@Nullable NetworkType networkType,
                                      @Nonnull String id);

    Flux<NetworkProperties> getAllConfigs();
}
