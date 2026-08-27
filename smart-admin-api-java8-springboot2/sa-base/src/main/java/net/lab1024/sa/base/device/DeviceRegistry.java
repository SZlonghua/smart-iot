package net.lab1024.sa.base.device;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceRegistry {

    Mono<DeviceOperator> getDevice(String deviceId);

    /** 获取设备操作对象（不校验存在性）— 离线清理等场景：产品被禁用/删除时 exist()=false，但 Redis 上线字段残留仍需清理 */
    Mono<DeviceOperator> getDeviceIgnoreExist(String deviceId);

    Mono<DeviceOperator> getDevice(String productKey, String deviceKey);

    Mono<DeviceProductOperator> getProduct(String productId);

    Flux<DeviceOperator> getDevicesByProduct(String productId);

    Mono<DeviceOperator> register(DeviceInfo deviceInfo);

    Mono<DeviceProductOperator> register(ProductInfo productInfo);

    Mono<Void> unregisterDevice(String deviceId);

    Mono<Void> unregisterProduct(String productId);
}
