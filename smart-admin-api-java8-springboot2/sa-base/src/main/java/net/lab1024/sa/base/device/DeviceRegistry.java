package net.lab1024.sa.base.device;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceRegistry {

    Mono<DeviceOperator> getDevice(String deviceId);

    Mono<DeviceOperator> getDevice(String productKey, String deviceKey);

    Mono<DeviceProductOperator> getProduct(String productId);

    Flux<DeviceOperator> getDevicesByProduct(String productId);

    Mono<DeviceOperator> register(DeviceInfo deviceInfo);

    Mono<DeviceProductOperator> register(ProductInfo productInfo);

    Mono<Void> unregisterDevice(String deviceId);

    Mono<Void> unregisterProduct(String productId);
}
