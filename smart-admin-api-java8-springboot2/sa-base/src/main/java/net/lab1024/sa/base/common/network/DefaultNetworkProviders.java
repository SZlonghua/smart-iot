package net.lab1024.sa.base.common.network;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NetworkProviders 默认实现。
 *
 * @Author 廖涛
 * @Date 2026/08/01
 * @Copyright 1024创新实验室
 */
public class DefaultNetworkProviders<C extends NetworkConfig> implements NetworkProviders<C> {

    private final Map<String, NetworkProvider<C>> registry = new ConcurrentHashMap<>();

    public DefaultNetworkProviders(List<NetworkProvider<C>> providerList) {
        providerList.forEach(p -> registry.put(p.getId(), p));
    }

    @Override
    public Mono<NetworkProvider<C>> getProvider(String provider) {
        return Mono.justOrEmpty(registry.get(provider));
    }

    @Override
    public Flux<NetworkProvider<C>> getProviders() {
        return Flux.fromIterable(registry.values());
    }

    @Override
    public Mono<Void> addNetworkProvider(NetworkProvider<C> provider) {
        registry.put(provider.getId(), provider);
        return Mono.empty();
    }
}
