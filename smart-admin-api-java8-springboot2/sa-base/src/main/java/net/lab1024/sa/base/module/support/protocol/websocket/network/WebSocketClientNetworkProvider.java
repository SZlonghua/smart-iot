package net.lab1024.sa.base.module.support.protocol.websocket.network;
import javax.annotation.Nonnull;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;
public class WebSocketClientNetworkProvider implements NetworkProvider<WebSocketClientConfig> {
    @Nonnull @Override public String getId() { return "ws-client"; }
    @Nonnull @Override public NetworkType getType() { return DefaultNetworkType.WEB_SOCKET_CLIENT; }
    @Nonnull @Override public Mono<WebSocketClientConfig> createConfig(@Nonnull NetworkProperties properties) {
        WebSocketClientConfig cfg = new WebSocketClientConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }
    @Nonnull @Override public Mono<Network> createNetwork(@Nonnull WebSocketClientConfig config) { return Mono.just(new VertxWebSocketClientNetwork()); }
    @Override public Mono<Network> reload(@Nonnull Network network, @Nonnull WebSocketClientConfig config) { return Mono.just(network); }
}
