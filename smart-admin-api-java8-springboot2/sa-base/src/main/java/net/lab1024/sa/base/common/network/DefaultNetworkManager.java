package net.lab1024.sa.base.common.network;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.module.support.eventbus.core.EventHandler;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class DefaultNetworkManager implements NetworkManager {

    private final Map<String, ConcurrentMap<String, Network>> store = new ConcurrentHashMap<>();
    private final NetworkProviders<? extends NetworkConfig> providers;
    private final NetworkConfigManager configManager;
    private final IEventBus eventBus;
    private volatile boolean initialized;

    public DefaultNetworkManager(NetworkProviders<? extends NetworkConfig> providers,
                                 NetworkConfigManager configManager,
                                 IEventBus eventBus) {
        this.providers = providers;
        this.configManager = configManager;
        this.eventBus = eventBus;
    }

    public void init() {
        if (initialized) return;
        initialized = true;
        eventBus.subscribe(new EventHandler<NetworkConfigEvent>() {
            @Override
            public void handle(NetworkConfigEvent event) {
                NetworkProperties props = event.getProperties();
                NetworkType type = NetworkType.of(props.getType());
                switch (event.getType()) {
                    case reload:
                        reload(type, props.getId()).subscribe();
                        break;
                    case destroy:
                        destroy(type, props.getId()).subscribe();
                        break;
                }
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Network> Mono<T> getNetwork(NetworkType type, String id) {
        if (id == null) {
            return Mono.empty();
        }
        ConcurrentMap<String, Network> networkStore = getNetworkStore(type.getId());
        return Mono.justOrEmpty(networkStore.get(id))
                .switchIfEmpty(doCreate(type, id))
                .doOnNext(network -> networkStore.putIfAbsent(id, network))
                .map(n -> (T) n);
    }

    @Override
    public Flux<Network> getNetworks() {
        return Flux.fromIterable(store.values())
                .flatMap(m -> Flux.fromIterable(m.values()));
    }

    @Override
    public Mono<Void> reload(NetworkType type, String id) {
        log.info("reload network {}", id);
        ConcurrentMap<String, Network> networkStore = getNetworkStore(type.getId());
        return Mono.justOrEmpty(networkStore.get(id))
                .flatMap(existing -> {
                    log.info("doReload network {}", id);
                    return doReload(existing, type, id);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("doCreate network {}", id);
                    return doCreate(type, id);
                }))
                .doOnNext(network -> networkStore.put(id, network))
                .doOnNext(Network::start)
                .then();
    }

    @Override
    public Mono<Void> shutdown(NetworkType type, String id) {
        return Mono.justOrEmpty(getNetworkStore(type.getId()).get(id))
                .doOnNext(Network::shutdown)
                .then();
    }

    @Override
    public Mono<Void> destroy(NetworkType type, String id) {
        return Mono.justOrEmpty(getNetworkStore(type.getId()).remove(id))
                .doOnNext(Network::shutdown)
                .then();
    }

    private Mono<Network> doReload(Network network, NetworkType type, String id) {
        return configManager.getConfig(type, id)
                .flatMap(props -> Mono.zip(
                        providers.getProvider(props.getType()),
                        Mono.just(props)))
                .flatMap(tp -> {
                    NetworkProvider<NetworkConfig> provider = cast(tp.getT1());
                    NetworkProperties networkProperties = tp.getT2();
                    return provider.createConfig(networkProperties)
                            .flatMap(networkConfig -> provider.reload(network, networkConfig));
                });
    }

    private Mono<Network> doCreate(NetworkType type, String id) {
        return configManager.getConfig(type, id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("网络组件配置不存在, 创建网络失败 — type={}, componentId={}", type.getId(), id);
                    return Mono.empty();
                }))
                .flatMap(props -> Mono.zip(
                        providers.getProvider(props.getType())
                                .switchIfEmpty(Mono.error(() ->
                                        new UnsupportedOperationException("网络组件Provider[" + props.getType() + "]不存在"))),
                        Mono.just(props)))
                .flatMap(tp -> {
                    NetworkProvider<NetworkConfig> provider = cast(tp.getT1());
                    NetworkProperties networkProperties = tp.getT2();
                    return provider.createConfig(networkProperties)
                            .flatMap(provider::createNetwork);
                });
    }

    private ConcurrentMap<String, Network> getNetworkStore(String typeId) {
        return store.computeIfAbsent(typeId, k -> new ConcurrentHashMap<>());
    }

    @SuppressWarnings("unchecked")
    private static <C extends NetworkConfig> NetworkProvider<C> cast(NetworkProvider<?> p) {
        return (NetworkProvider<C>) p;
    }
}
