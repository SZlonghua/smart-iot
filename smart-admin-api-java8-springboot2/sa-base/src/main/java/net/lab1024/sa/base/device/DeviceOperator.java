package net.lab1024.sa.base.device;

import net.lab1024.sa.base.metadata.ThingsMetadata;
import net.lab1024.sa.base.module.support.cache.core.Value;
import net.lab1024.sa.base.storage.StorageStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;

public interface DeviceOperator {

    String getDeviceId();

    Mono<String> getConnectionServerId();

    Mono<Value> getSelfConfig(String key);

    Flux<Value> getSelfConfigs(Collection<String> keys);

    default Flux<Value> getSelfConfigs(String... keys) {
        return getSelfConfigs(Arrays.asList(keys));
    }

    default Mono<java.util.List<Value>> getSelfConfigValues(String... keys) {
        return getSelfConfigs(keys).collectList();
    }

    Mono<AuthenticationResponse> authenticate(DeviceAuthenticationRequest request);

    Mono<ThingsMetadata> getMetadata();

    Mono<DeviceProductOperator> getProduct();

    /**
     * 取设备消息数据存储策略 — 经所属产品按存储策略 ID 解析；设备未绑定产品/产品不存在、存储未开启/
     * 无匹配策略一律兜底 EmptyStorageStrategy，Mono 恒非空、返回恒非 null
     */
    Mono<StorageStrategy> getStorageStrategy();

    Boolean exist();

    /** 批量写入自身配置 */
    void setConfigs(java.util.Map<String, Object> values);

    /** 删除自身配置项 */
    void removeConfigs(String... keys);

    /** 清空自身全部配置 */
    void clear();
}
