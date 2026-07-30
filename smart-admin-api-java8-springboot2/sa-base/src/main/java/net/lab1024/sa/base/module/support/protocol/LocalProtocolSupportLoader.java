package net.lab1024.sa.base.module.support.protocol;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDefinition;
import net.lab1024.sa.base.common.protocol.ProtocolSupportLoader;
import reactor.core.publisher.Mono;

/**
 * 本地协议加载器 — 从当前 classpath 加载 ProtocolSupport 实现。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
@Slf4j
public class LocalProtocolSupportLoader implements ProtocolSupportLoader {

    @Override
    public boolean supports(ProtocolSupportDefinition definition) {
        return "local".equals(definition.getLoader());
    }

    @Override
    public Mono<? extends ProtocolSupport> load(ProtocolSupportDefinition definition) {
        log.info("[LocalLoader] 加载本地协议 — id={}, configuration={}",
                definition.getId(), definition.getConfiguration());
        // TODO: 后续实现本地加载逻辑
        return Mono.empty();
    }
}
