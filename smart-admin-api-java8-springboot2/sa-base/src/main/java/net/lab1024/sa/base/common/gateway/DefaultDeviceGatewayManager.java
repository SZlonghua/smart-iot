package net.lab1024.sa.base.common.gateway;

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

    private final ConcurrentHashMap<String, DeviceGateway> registry = new ConcurrentHashMap<>();
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
        return Mono.justOrEmpty(registry.get(id))
                .switchIfEmpty(createGateway(id))
                .doOnNext(gateway -> registry.putIfAbsent(id, gateway));
    }

    protected Mono<DeviceGateway> createGateway(String id) {
        log.info("create gateway {}", id);
        return propertiesManager.getProperties(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("网关配置不存在, 创建网关失败 — gatewayId={}", id);
                    return Mono.empty();
                }))
                .flatMap(properties -> providers.getProvider(properties.getProvider())
                        .switchIfEmpty(Mono.defer(() -> {
                            log.warn("网关Provider不存在, 创建网关失败 — provider={}, gatewayId={}", properties.getProvider(), id);
                            return Mono.empty();
                        }))
                        .flatMap(provider -> provider.createDeviceGateway(properties)))
                .cast(DeviceGateway.class);
    }

    @Override
    public Mono<Void> reload(String gatewayId) {
        log.info("reload gateway {}", gatewayId);
        return doReload(gatewayId);
    }

    private Mono<Void> doReload(String gatewayId) {
        return Mono.justOrEmpty(registry.get(gatewayId))
                .flatMap(this::reloadGateway)
                .switchIfEmpty(Mono.defer(() -> createGateway(gatewayId)))
                .doOnNext(gateway -> registry.put(gatewayId, gateway))
                .doOnNext(DeviceGateway::startup)
                .then()
                .doOnSuccess(nil -> log.info("reload gateway {} 完成", gatewayId))
                .doOnError(err -> log.error("reload gateway {} 失败", gatewayId, err));
    }

    private Mono<DeviceGateway> reloadGateway(DeviceGateway gateway) {
        log.info("reloadGateway gateway:{}", gateway);
        return propertiesManager.getProperties(gateway.getId())
                .switchIfEmpty(Mono.error(() ->
                        new UnsupportedOperationException("网关配置[" + gateway.getId() + "]不存在")))
                .flatMap(properties -> providers.getProvider(properties.getProvider())
                        .switchIfEmpty(Mono.error(() ->
                                new UnsupportedOperationException("网关Provider[" + properties.getProvider() + "]不存在")))
                        .flatMap(provider -> provider.reloadDeviceGateway(gateway, properties)));
    }

    /*private Mono<Void> doReload(String gatewayId) {
        log.info("doReload gatewayId:{}", gatewayId);
        return Mono.justOrEmpty(registry.get(gatewayId))
                .flatMap(existing -> propertiesManager.getProperties(gatewayId)
                        .flatMap(properties -> providers.getProvider(properties.getProvider())
                                .flatMap(provider -> provider.reloadDeviceGateway(existing, properties))))
                .switchIfEmpty(Mono.defer(() -> propertiesManager.getProperties(gatewayId)
                        .flatMap(properties -> providers.getProvider(properties.getProvider())
                                .flatMap(provider -> provider.createDeviceGateway(properties)
                                        .flatMap(g -> g.startup().thenReturn(g))))))
                .doOnNext(gateway -> registry.put(gatewayId, gateway))
                .then();
    }*/

    @Override
    public Mono<Void> start(String id) {
        return getGateway(id)
                .flatMap(DeviceGateway::startup)
                .doOnSuccess(nil -> log.info("started device gateway {}", id))
                .doOnError(err -> log.error("start device gateway {} error", id, err));
    }

    @Override
    public Mono<Void> shutdown(String gatewayId) {
        return Mono.justOrEmpty(registry.remove(gatewayId))
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
        DeviceGateway removed = registry.remove(props.getId());
        if (removed != null) {
            removed.shutdown().subscribe();
            log.info("[GatewayManager] 网关已注销 — id={}", props.getId());
        }
    }


}
