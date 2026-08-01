package net.lab1024.sa.base.module.support.protocol.gateway;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.gateway.*;
import net.lab1024.sa.base.module.support.eventbus.core.EventHandler;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

/**
 * DeviceGatewayManager 默认实现。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
@Slf4j
public class DefaultDeviceGatewayManager implements DeviceGatewayManager {

    private final ConcurrentHashMap<String, Mono<DeviceGateway>> registry = new ConcurrentHashMap<>();
    private final DeviceGatewayProviders providers;
    private final DeviceGatewayPropertiesManager propertiesManager;
    private final IEventBus eventBus;

    public DefaultDeviceGatewayManager(DeviceGatewayProviders providers,
                                        DeviceGatewayPropertiesManager propertiesManager,
                                        IEventBus eventBus) {
        this.providers = providers;
        this.propertiesManager = propertiesManager;
        this.eventBus = eventBus;
    }

    @Override
    public void init() {
        eventBus.subscribe(new EventHandler<DeviceGatewayEvent>() {
            @Override
            public void handle(DeviceGatewayEvent event) {
                switch (event.getType()) {
                    case register:
                        onRegister(event.getProperties());
                        break;
                    case reload:
                        onReload(event.getProperties());
                        break;
                    case unregister:
                        onUnregister(event.getProperties());
                        break;
                }
            }
        });
    }

    @Override
    public Mono<Void> loadAll() {
        return propertiesManager.getAllProperties()
                .filter(DeviceGatewayProperties::isEnabled)
                .flatMap(p -> start(p.getId()))
                .then();
    }

    @Override
    public Mono<DeviceGateway> getGateway(String id) {
        if (id == null) {
            return Mono.empty();
        }
        return registry.computeIfAbsent(id, this::createGateway);
    }

    protected Mono<DeviceGateway> createGateway(String id) {
        return propertiesManager.getProperties(id)
                .switchIfEmpty(Mono.error(() ->
                        new UnsupportedOperationException("网关配置[" + id + "]不存在")))
                .flatMap(properties -> providers.getProvider(properties.getProvider())
                        .switchIfEmpty(Mono.error(() ->
                                new UnsupportedOperationException("网关Provider[" + properties.getProvider() + "]不存在")))
                        .flatMap(provider -> provider.createDeviceGateway(properties)))
                .cast(DeviceGateway.class);
    }

    @Override
    public Mono<Void> reload(String gatewayId) {
        return doReload(gatewayId);
    }

    private Mono<Void> doReload(String gatewayId) {
        return propertiesManager.getProperties(gatewayId)
                .flatMap(properties -> providers.getProvider(properties.getProvider())
                        .flatMap(provider -> registry.compute(gatewayId, (id, gateway) -> {
                            if (gateway != null) {
                                return gateway.flatMap(g -> provider.reloadDeviceGateway(g, properties));
                            }
                            return provider.createDeviceGateway(properties)
                                    .flatMap(g -> g.startup().thenReturn(g));
                        })))
                .then();
    }

    @Override
    public Mono<Void> start(String id) {
        return getGateway(id)
                .flatMap(DeviceGateway::startup)
                .doOnSuccess(nil -> log.debug("started device gateway {}", id))
                .doOnError(err -> log.error("start device gateway {} error", id, err));
    }

    @Override
    public Mono<Void> shutdown(String gatewayId) {
        return registry.remove(gatewayId)
                .flatMap(DeviceGateway::shutdown)
                .doOnSuccess(nil -> log.debug("shutdown device gateway {}", gatewayId))
                .doOnError(err -> log.error("shutdown device gateway {} error", gatewayId, err));
    }

    private void onRegister(DeviceGatewayProperties props) {
        reload(props.getId()).subscribe();
    }

    private void onReload(DeviceGatewayProperties props) {
        reload(props.getId()).subscribe();
    }

    private void onUnregister(DeviceGatewayProperties props) {
        Mono<DeviceGateway> removed = registry.remove(props.getId());
        if (removed != null) {
            removed.flatMap(DeviceGateway::shutdown).subscribe();
            log.info("[GatewayManager] 网关已注销 — id={}", props.getId());
        }
    }


}
