package net.lab1024.sa.base.common.protocol;

import reactor.core.publisher.Mono;

public interface ProtocolSupportLoader {

    /** 是否支持该定义 */
    boolean supports(ProtocolSupportDefinition definition);

    /** 加载协议实例 */
    Mono<? extends ProtocolSupport> load(ProtocolSupportDefinition definition);
}
