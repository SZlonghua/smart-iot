package net.lab1024.sa.base.device.support;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.device.*;
import net.lab1024.sa.base.module.support.cache.core.*;
import net.lab1024.sa.base.common.util.SmartMapUtil;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.TimeUnit;




/**
 * DeviceRegistry 默认实现。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Slf4j
public class DefaultDeviceRegistry implements DeviceRegistry {


    private static final String KEY_PREFIX_BIND = "device-product-bind:";
    private static final String KEY_PREFIX_MAPPING = "mapping:deviceId:";

    private final Map<String, Mono<DeviceOperator>> deviceOperatorCache;
    private final Map<String, Mono<DeviceProductOperator>> productOperatorCache;

    private final IConfigStorageManager configStorageManager;
    private final ICacheManager cacheManager;
    private final IEventBus eventBus;

    public DefaultDeviceRegistry(IConfigStorageManager configStorageManager,
                                 ICacheManager cacheManager,
                                 IEventBus eventBus) {
        this.configStorageManager = configStorageManager;
        this.cacheManager = cacheManager;
        this.eventBus = eventBus;
        this.deviceOperatorCache = Caffeine.newBuilder()
                .softValues()
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .<String, Mono<DeviceOperator>>build()
                .asMap();
        this.productOperatorCache = Caffeine.newBuilder()
                .softValues()
                .<String, Mono<DeviceProductOperator>>build()
                .asMap();
    }

    @Override
    public Mono<DeviceOperator> getDevice(String deviceId) {
        return Mono.justOrEmpty(deviceId)
                .filter(StringUtils::isNotEmpty)
                .flatMap(id -> deviceOperatorCache.getOrDefault(id, Mono.empty()))
                .filter(DeviceOperator::exist)
                .switchIfEmpty(Mono.defer(() -> reloadAndCache(deviceId)));

    }

    /** 重新加载设备并写入缓存，不存在则返回 empty */
    private Mono<DeviceOperator> reloadAndCache(String deviceId) {
        return Mono.justOrEmpty(deviceId)
                .doOnNext(deviceOperatorCache::remove)
                .map(this::createOperator)
                .filter(DeviceOperator::exist)
                .flatMap(operator -> deviceOperatorCache.computeIfAbsent(deviceId, k -> Mono.just(operator)));
    }

    private DeviceOperator createOperator(String deviceId) {
        return new DefaultDeviceOperator(deviceId, configStorageManager, eventBus, this);
    }

    @Override
    public Mono<DeviceOperator> getDevice(String productKey, String deviceKey) {
        return Mono.justOrEmpty(cacheManager.getStringCache()
                        .get(KEY_PREFIX_MAPPING + productKey + "-" + deviceKey))
                .flatMap(this::getDevice);
    }

    @Override
    public Flux<DeviceOperator> getDevicesByProduct(String productId) {
        return Flux.fromIterable(cacheManager.getSetCache(
                        KEY_PREFIX_BIND + productId).members())
                .flatMap(this::getDevice);
    }

    @Override
    public Mono<DeviceProductOperator> getProduct(String productId) {
        return Mono.justOrEmpty(productId)
                .filter(StringUtils::isNotEmpty)
                .flatMap(id -> productOperatorCache.getOrDefault(id, Mono.empty()))
                .switchIfEmpty(Mono.defer(() -> reloadProductAndCache(productId)));
    }

    /** 重新加载产品并写入缓存，不存在则返回 empty */
    private Mono<DeviceProductOperator> reloadProductAndCache(String productId) {
        return Mono.justOrEmpty(productId)
                .doOnNext(productOperatorCache::remove)
                .map(this::createProductOperator)
                .filter(DeviceProductOperator::exist)
                .flatMap(operator -> productOperatorCache.computeIfAbsent(productId, k -> Mono.just(operator)));
    }

    private DeviceProductOperator createProductOperator(String productId) {
        return new DefaultDeviceProductOperator(productId, configStorageManager, cacheManager, eventBus, this);
    }

    @Override
    public Mono<DeviceOperator> register(DeviceInfo deviceInfo) {
        return Mono.just(deviceInfo)
                .doOnNext(info -> deviceOperatorCache.remove(info.getDeviceId()))
                .map(info -> createOperator(info.getDeviceId()))
                // 1. 写入设备自身配置
                .doOnNext(operator -> operator.setConfigs(buildDeviceConfigMap(deviceInfo)))
                // 2. 产品→设备绑定
                .doOnNext(operator -> cacheManager.getSetCache(
                        KEY_PREFIX_BIND + deviceInfo.getProductId()).add(deviceInfo.getDeviceId()))
                // 3. deviceKey→deviceId 映射
                .doOnNext(operator -> cacheManager.getStringCache().set(
                        KEY_PREFIX_MAPPING + deviceInfo.getProductKey() + "-" + deviceInfo.getDeviceKey(),
                        deviceInfo.getDeviceId()));
    }

    @Override
    public Mono<DeviceProductOperator> register(ProductInfo productInfo) {
        return Mono.just(productInfo)
                .doOnNext(info -> productOperatorCache.remove(info.getProductId()))
                .map(info -> createProductOperator(info.getProductId()))
                .doOnNext(operator -> operator.setConfigs(buildProductConfigMap(productInfo)));
    }

    @Override
    public Mono<Void> unregisterDevice(String deviceId) {
        return Mono.justOrEmpty(deviceOperatorCache.remove(deviceId))
                .flatMap(m -> m)
                .switchIfEmpty(Mono.fromCallable(() -> createOperator(deviceId)))
                .flatMap(operator -> Mono.zip(
                        Mono.just(operator),
                        operator.getSelfConfigValues(
                                DeviceField.PRODUCT_ID.getValue(),
                                DeviceField.PRODUCT_KEY.getValue(),
                                DeviceField.DEVICE_KEY.getValue()
                        )
                ))
                // 1. 清理产品→设备绑定
                .doOnNext(tuple -> {
                    String productId = tuple.getT2().get(0).asString();
                    if (productId != null) {
                        cacheManager.getSetCache(KEY_PREFIX_BIND + productId).remove(deviceId);
                    }
                })
                // 2. 清理 deviceKey→deviceId 映射
                .doOnNext(tuple -> {
                    String productKey = tuple.getT2().get(1).asString();
                    String deviceKey = tuple.getT2().get(2).asString();
                    if (productKey != null && deviceKey != null) {
                        cacheManager.getStringCache().delete(
                                KEY_PREFIX_MAPPING + productKey + "-" + deviceKey);
                    }
                })
                // 3. 清空设备自身配置
                .doOnNext(tuple -> tuple.getT1().clear())
                .then();
    }

    @Override
    public Mono<Void> unregisterProduct(String productId) {
        return Mono.justOrEmpty(productOperatorCache.remove(productId))
                .flatMap(m -> m)
                .switchIfEmpty(Mono.fromCallable(() -> createProductOperator(productId)))
                .doOnNext(DeviceProductOperator::clear)
                .then();
    }

    // ========== helper ==========

    private static Map<String, Object> buildProductConfigMap(ProductInfo info) {
        return SmartMapUtil.builder()
                .put(ProductField.PRODUCT_NAME.getValue(), info.getProductName())
                .put(ProductField.METADATA.getValue(), info.getMetadata())
                .put(ProductField.PRODUCT_KEY.getValue(), info.getProductKey())
                .put(ProductField.PRODUCT_SECRET.getValue(), info.getProductSecret())
                .put(ProductField.IS_GATEWAY_DEVICE.getValue(), info.getIsGatewayDevice() != null
                        && info.getIsGatewayDevice() ? Boolean.TRUE.toString() : Boolean.FALSE.toString())
                .build();
    }

    private static Map<String, Object> buildDeviceConfigMap(DeviceInfo info) {
        return SmartMapUtil.builder()
                .put(DeviceField.DEVICE_NAME.getValue(), info.getDeviceName())
                .put(DeviceField.DEVICE_KEY.getValue(), info.getDeviceKey())
                .put(DeviceField.DEVICE_SECRET.getValue(), info.getDeviceSecret())
                .put(DeviceField.PRODUCT_ID.getValue(), info.getProductId())
                .put(DeviceField.PRODUCT_NAME.getValue(), info.getProductName())
                .put(DeviceField.PRODUCT_KEY.getValue(), info.getProductKey())
                .put(DeviceField.PRODUCT_SECRET.getValue(), info.getProductSecret())
//                .put(DeviceField.SESSION_ID.getValue(), info.getSessionId())
//                .put(DeviceField.PROTOCOL_ID.getValue(), info.getProtocolId())
                .put(DeviceField.GATEWAY_ID.getValue(), info.getGatewayId())
                .put(DeviceField.CONNECTION_SERVER_ID.getValue(), info.getConnectionServerId())
                .build();
    }
}
