package net.lab1024.sa.base.device.support;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.device.*;
import net.lab1024.sa.base.metadata.ThingsMetadata;
import net.lab1024.sa.base.module.support.cache.core.ICacheManager;
import net.lab1024.sa.base.module.support.thingsmodel.IotThingsMetadataCodec;
import net.lab1024.sa.base.module.support.cache.core.IConfigStorage;
import net.lab1024.sa.base.module.support.cache.core.IConfigStorageManager;
import net.lab1024.sa.base.module.support.cache.core.Value;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

/**
 * DeviceProductOperator 默认实现 — 基于 IConfigStorage。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Slf4j
public class DefaultDeviceProductOperator implements DeviceProductOperator {

    private static final String KEY_PREFIX_PRODUCT = "device-product:";

    private final String productId;
    private final IConfigStorage storage;
    private final IConfigStorageManager configStorageManager;
    private final ICacheManager cacheManager;
    private final IEventBus eventBus;
    private final DeviceRegistry registry;

    public DefaultDeviceProductOperator(String productId,
                                         IConfigStorageManager configStorageManager,
                                         ICacheManager cacheManager,
                                         IEventBus eventBus,
                                         DeviceRegistry registry) {
        this.productId = productId;
        this.storage = configStorageManager.getStorage(KEY_PREFIX_PRODUCT + productId);
        this.configStorageManager = configStorageManager;
        this.cacheManager = cacheManager;
        this.eventBus = eventBus;
        this.registry = registry;
    }

    @Override
    public String getId() {
        return productId;
    }

    @Override
    public Mono<ThingsMetadata> getMetadata() {
        Value meta = storage.getConfig(ProductField.METADATA.getValue());
        if (!meta.isPresent()) {
            return Mono.empty();
        }
        return Mono.just(IotThingsMetadataCodec.getInstance().decode(meta.asString()));
    }

    @Override
    public Mono<Boolean> updateMetadata(String metadata) {
        storage.setConfig(ProductField.METADATA.getValue(), metadata);
        return Mono.just(true);
    }

    @Override
    public Flux<DeviceOperator> getDevices() {
        return registry.getDevicesByProduct(productId);
    }

    @Override
    public void setConfigs(Map<String, Object> values) {
        storage.setConfigs(values);
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public Mono<Value> getSelfConfig(String key) {
        return Mono.just(storage.getConfig(key));
    }

    @Override
    public Flux<Value> getSelfConfigs(Collection<String> keys) {
        return Flux.fromIterable(storage.getConfigs(keys));
    }

    @Override
    public Boolean exist() {
        return storage.size() > 0;
    }
}
