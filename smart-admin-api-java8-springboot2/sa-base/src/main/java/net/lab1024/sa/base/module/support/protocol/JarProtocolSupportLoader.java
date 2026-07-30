package net.lab1024.sa.base.module.support.protocol;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDefinition;
import net.lab1024.sa.base.common.protocol.ProtocolSupportLoader;
import reactor.core.publisher.Mono;

/**
 * Jar 协议加载器 — 从外部 jar 包加载 ProtocolSupport 实现。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
@Slf4j
public class JarProtocolSupportLoader implements ProtocolSupportLoader {

    @Override
    public boolean supports(ProtocolSupportDefinition definition) {
        return "jar".equals(definition.getLoader());
    }

    @Override
    public Mono<? extends ProtocolSupport> load(ProtocolSupportDefinition definition) {
        log.info("[JarLoader] 加载协议 — id={}, jarPath={}",
                definition.getId(), definition.getConfiguration());
        // TODO: 后续实现 jar 加载逻辑
        return Mono.empty();
    }
}
