package net.lab1024.sa.base.device;

import net.lab1024.sa.base.metadata.ThingsMetadata;
import net.lab1024.sa.base.module.support.cache.core.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface DeviceProductOperator {

    String getId();

    Mono<ThingsMetadata> getMetadata();

    Mono<Boolean> updateMetadata(String metadata);

    Flux<DeviceOperator> getDevices();

    Boolean exist();

    Mono<Value> getSelfConfig(String key);

    Flux<Value> getSelfConfigs(Collection<String> keys);

    default Flux<Value> getSelfConfigs(String... keys) {
        return getSelfConfigs(Arrays.asList(keys));
    }

    default Mono<List<Value>> getSelfConfigValues(String... keys) {
        return getSelfConfigs(keys).collectList();
    }

    /** 批量写入自身配置 */
    void setConfigs(java.util.Map<String, Object> values);

    /** 清空自身全部配置 */
    void clear();
}
