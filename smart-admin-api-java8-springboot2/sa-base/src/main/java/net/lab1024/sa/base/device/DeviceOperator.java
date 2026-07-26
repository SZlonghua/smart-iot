package net.lab1024.sa.base.device;

import net.lab1024.sa.base.metadata.ThingsMetadata;
import net.lab1024.sa.base.module.support.cache.core.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;

public interface DeviceOperator {

    String getDeviceId();

    Mono<String> getConnectionServerId();

    Mono<String> getSessionId();

    Mono<Long> getOnlineTime();

    Mono<Long> getOfflineTime();

    Mono<Value> getSelfConfig(String key);

    Flux<Value> getSelfConfigs(Collection<String> keys);

    default Flux<Value> getSelfConfigs(String... keys) {
        return getSelfConfigs(Arrays.asList(keys));
    }

    default Mono<java.util.List<Value>> getSelfConfigValues(String... keys) {
        return getSelfConfigs(keys).collectList();
    }

    Mono<Boolean> disconnect();

    Mono<AuthenticationResponse> authenticate(AuthenticationRequest request);

    Mono<ThingsMetadata> getMetadata();

    Mono<DeviceProductOperator> getProduct();

    Boolean exist();

    /** 批量写入自身配置 */
    void setConfigs(java.util.Map<String, Object> values);

    /** 清空自身全部配置 */
    void clear();
}
