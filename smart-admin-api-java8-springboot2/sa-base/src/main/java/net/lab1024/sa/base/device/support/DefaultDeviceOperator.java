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
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/07/22
 * &#064;Copyright  1024创新实验室
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
    public Mono<AuthenticationResponse> authenticate(DeviceAuthenticationRequest request) {
        return Mono.fromCallable(() -> {
            // 1. 防重放：|now - timestamp| ≤ 5分钟
            /*long now = System.currentTimeMillis();
            if (Math.abs(now - request.getTimestamp()) > 300_000) {
                return AuthenticationResponse.error(401, "时间戳超时");
            }*/

            // 2. 读取设备密钥
            String deviceSecret = storage.getConfig(DeviceField.DEVICE_SECRET.getValue()).asString();
            if (deviceSecret == null) {
                return AuthenticationResponse.error(402, "设备密钥不存在");
            }

            // 3. 读取产品密钥（一型一密时需要）
            String productSecret = null;
            if (request.getMode() == 2) {
                productSecret = storage.getConfig(DeviceField.PRODUCT_SECRET.getValue()).asString();
                if (productSecret == null) {
                    return AuthenticationResponse.error(402, "产品密钥不存在");
                }
            }

            // 4. 用服务端密钥构建期望签名 → 比对
            DeviceAuthenticationRequest expected = request.copy(deviceSecret, productSecret);
            if (!request.isSignatureMatch(expected)) {
                return AuthenticationResponse.error(403, "签名验证失败");
            }

            return AuthenticationResponse.success(deviceId);
        });
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
    public void removeConfigs(String... keys) {
        storage.remove(keys);
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
