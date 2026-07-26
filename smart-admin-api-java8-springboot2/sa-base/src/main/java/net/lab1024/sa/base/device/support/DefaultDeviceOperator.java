package net.lab1024.sa.base.device.support;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.device.*;
import net.lab1024.sa.base.metadata.ThingsMetadata;
import net.lab1024.sa.base.module.support.cache.core.IConfigStorage;
import net.lab1024.sa.base.module.support.cache.core.IConfigStorageManager;
import net.lab1024.sa.base.module.support.cache.core.Value;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

/**
 * DeviceOperator 默认实现 — 基于 IConfigStorage。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Slf4j
public class DefaultDeviceOperator implements DeviceOperator {

    private static final String KEY_PREFIX_DEVICE = "device:";

    private final String deviceId;
    private final IConfigStorage storage;
    private final IConfigStorageManager configStorageManager;
    private final IEventBus eventBus;
    private final DeviceRegistry registry;

    public DefaultDeviceOperator(String deviceId, IConfigStorageManager configStorageManager,
                                  IEventBus eventBus, DeviceRegistry registry) {
        this.deviceId = deviceId;
        this.storage = configStorageManager.getStorage(KEY_PREFIX_DEVICE + deviceId);
        this.configStorageManager = configStorageManager;
        this.eventBus = eventBus;
        this.registry = registry;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public Mono<String> getConnectionServerId() {
        return Mono.justOrEmpty(storage.getConfig(DeviceField.CONNECTION_SERVER_ID.getValue()).asString());
    }

    @Override
    public Mono<String> getSessionId() {
        return Mono.justOrEmpty(storage.getConfig(DeviceField.SESSION_ID.getValue()).asString());
    }

    @Override
    public Mono<Long> getOnlineTime() {
        Value v = storage.getConfig(DeviceField.ONLINE_TIME.getValue());
        return v.isPresent() ? Mono.just(v.asLong()) : Mono.empty();
    }

    @Override
    public Mono<Long> getOfflineTime() {
        Value v = storage.getConfig(DeviceField.OFFLINE_TIME.getValue());
        return v.isPresent() ? Mono.just(v.asLong()) : Mono.empty();
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
    public Mono<Boolean> disconnect() {
        // 预留还未实现
        storage.setConfig(DeviceField.SESSION_ID.getValue(), "");
        return Mono.just(true);
    }

    @Override
    public Mono<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        String storedKey = storage.getConfig(DeviceField.DEVICE_KEY.getValue()).asString();
        String storedSecret = storage.getConfig(DeviceField.DEVICE_SECRET.getValue()).asString();

        boolean success = storedKey != null && storedKey.equals(request.getDeviceKey())
                && storedSecret != null && storedSecret.equals(request.getDeviceSecret());

        return Mono.just(AuthenticationResponse.builder()
                .success(success)
                .deviceId(deviceId)
                .message(success ? "认证成功" : "认证失败")
                .build());
    }

    @Override
    public Mono<ThingsMetadata> getMetadata() {
        return getProduct().flatMap(DeviceProductOperator::getMetadata);
    }

    @Override
    public Mono<DeviceProductOperator> getProduct() {
        String productId = storage.getConfig(DeviceField.PRODUCT_ID.getValue()).asString();
        if (productId == null) {
            return Mono.empty();
        }
        return registry.getProduct(productId);
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
    public Boolean exist() {
        if (storage.size() == 0) {
            return false;
        }
        return getProduct()
                .blockOptional()
                .filter(DeviceProductOperator::exist)
                .isPresent();
    }
}
