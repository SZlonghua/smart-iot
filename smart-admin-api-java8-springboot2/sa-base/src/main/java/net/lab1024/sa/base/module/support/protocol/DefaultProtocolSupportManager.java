package net.lab1024.sa.base.module.support.protocol;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDataService;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDefinition;
import net.lab1024.sa.base.common.protocol.ProtocolSupportEvent;
import net.lab1024.sa.base.common.protocol.ProtocolSupportLoaders;
import net.lab1024.sa.base.common.protocol.ProtocolSupportManager;
import net.lab1024.sa.base.module.support.eventbus.core.EventHandler;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.springframework.beans.factory.ObjectProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ProtocolSupportManager 默认实现 — 基于 ConcurrentHashMap 的协议注册中心。
 * 启动时加载所有协议，并通过 {@link ProtocolSupportEvent} 动态管理生命周期。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
@Slf4j
public class DefaultProtocolSupportManager implements ProtocolSupportManager {
    // 如果集群环境下 每个节点registry都保存了全部ProtocolSupport
    private final Map<String, ProtocolSupport> registry = new ConcurrentHashMap<>();
    private final ProtocolSupportLoaders loaders;
    private final IEventBus eventBus;
    private final ObjectProvider<ProtocolSupport> customProtocolSupport;
    private final ProtocolSupportDataService dataService;

    public DefaultProtocolSupportManager(ProtocolSupportLoaders loaders,
                                          IEventBus eventBus,
                                          ObjectProvider<ProtocolSupport> customProtocolSupport,
                                          ProtocolSupportDataService dataService) {
        this.loaders = loaders;
        this.eventBus = eventBus;
        this.customProtocolSupport = customProtocolSupport;
        this.dataService = dataService;
    }

    /** 由 Spring @Bean(initMethod = "init") 调用 */
    public void init() {
        eventBus.subscribe(new EventHandler<ProtocolSupportEvent>() {
            @Override
            public void handle(ProtocolSupportEvent event) {
                switch (event.getType()) {
                    case SAVED:
                        onSaved(event.getDefinition());
                        break;
                    case UPDATED:
                        onUpdated(event.getDefinition());
                        break;
                    case DELETED:
                        onDeleted(event.getDefinition());
                        break;
                }
            }
        });

        customProtocolSupport.forEach(support -> {
            registry.put(support.getId(), support);
            log.info("[ProtocolManager] 注册自定义协议 — id={}, name={}", support.getId(), support.getName());
        });
    }

    /** 服务启动完成后调用，此时 HTTP 端口已就绪 */
    public Mono<Void> loadAll() {
        return dataService.getAll()
                .flatMap(this::loadAndRegister)
                .then();
    }

    @Override
    public Mono<ProtocolSupport> getProtocol(String protocol) {
        return Mono.justOrEmpty(registry.get(protocol));
    }

    @Override
    public Flux<ProtocolSupport> getProtocols() {
        return Flux.fromIterable(registry.values());
    }

    @Override
    public Mono<Void> register(Mono<ProtocolSupport> protocolSupport) {
        return protocolSupport.flatMap(this::registerInternal);
    }

    /** 加载协议定义 → 注册，失败则静默跳过 */
    private Mono<Void> loadAndRegister(ProtocolSupportDefinition def) {
        return loaders.load(def)
                .flatMap(this::registerInternal)
                .onErrorResume(e -> {
                    log.warn("[ProtocolManager] 加载失败 — id={}, msg={}",
                            def.getId(), e.getMessage());
                    return Mono.empty();
                });
    }

    private Mono<Void> registerInternal(ProtocolSupport support) {
        registry.put(support.getId(), support);
        log.info("[ProtocolManager] 注册协议 — id={}, name={}", support.getId(), support.getName());
        return Mono.empty();
    }

    /** 保存 → 加载并注册 */
    private void onSaved(ProtocolSupportDefinition def) {
        loadAndRegister(def).subscribe();
    }

    /** 修改 → 注销旧 → 加载新 → 注册 */
    private void onUpdated(ProtocolSupportDefinition def) {
        ProtocolSupport old = registry.remove(def.getId());
        if (old != null) {
            old.dispose();
            log.info("[ProtocolManager] 协议已注销 — id={}", def.getId());
        }
        loadAndRegister(def).subscribe();
    }

    /** 删除 → 注销并关闭类加载器 */
    private void onDeleted(ProtocolSupportDefinition def) {
        ProtocolSupport removed = registry.remove(def.getId());
        log.info("[ProtocolManager] 协议已移除 — id={}", def.getId());
        if (removed != null) {
            removed.dispose();
            log.info("[ProtocolManager] 协议已移除 — id={}", def.getId());
        }
    }
}
