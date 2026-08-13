package net.lab1024.sa.base.common.gateway;

import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.common.gateway.DeviceGatewayProviders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DeviceGatewayProviders 默认实现。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
public class DefaultDeviceGatewayProviders implements DeviceGatewayProviders {

    private final Map<String, DeviceGatewayProvider> registry = new ConcurrentHashMap<>();

    public DefaultDeviceGatewayProviders(List<DeviceGatewayProvider> providerList) {
        providerList.forEach(p -> registry.put(p.getId(), p));
    }

    @Override
    public Mono<DeviceGatewayProvider> getProvider(String id) {
        return Mono.justOrEmpty(registry.get(id));
    }

    @Override
    public Flux<DeviceGatewayProvider> getProviders() {
        return Flux.fromIterable(registry.values());
    }

    @Override
    public Mono<Void> addGatewayProvider(DeviceGatewayProvider provider) {
        registry.put(provider.getId(), provider);
        return Mono.empty();
    }
}
