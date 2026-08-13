package net.lab1024.sa.base.module.support.protocol;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDefinition;
import net.lab1024.sa.base.common.protocol.ProtocolSupportLoader;
import net.lab1024.sa.base.common.protocol.ProtocolSupportLoaders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 复合协议加载器 — 遍历内部 Loader 列表，找到第一个 supports=true 的 Loader 并委托加载。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
@Slf4j
public class DefaultProtocolSupportLoaders implements ProtocolSupportLoaders {

    private final List<ProtocolSupportLoader> loaders;

    public DefaultProtocolSupportLoaders(List<ProtocolSupportLoader> loaders) {
        this.loaders = loaders;
    }

    @Override
    public boolean supports(ProtocolSupportDefinition definition) {
        return loaders.stream().anyMatch(l -> l.supports(definition));
    }

    @Override
    public void close(String id) {
        loaders.forEach(l -> l.close(id));
    }

    @Override
    public Mono<? extends ProtocolSupport> load(ProtocolSupportDefinition definition) {
        return Flux.fromIterable(loaders)
                .filter(loader -> loader.supports(definition))
                .next()
                .switchIfEmpty(Mono.error(() ->
                        new IllegalArgumentException("无匹配的加载器: loader=" + definition.getLoader())))
                .flatMap(loader -> loader.load(definition))
                .doOnSuccess(ps -> {
                    if (ps != null) {
                        log.info("[ProtocolLoaders] 加载成功 — id={}, loader={}, class={}",
                                ps.getId(), definition.getLoader(), ps.getClass().getName());
                    }
                })
                .doOnError(e -> log.error("[ProtocolLoaders] 加载失败（稍后可通过事件重试） — id={}, loader={}",
                        definition.getId(), definition.getLoader()));
    }
}
