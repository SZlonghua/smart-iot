package net.lab1024.sa.base.module.support.protocol.websocket.network;
import javax.annotation.Nonnull;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;
public class WebSocketServerNetworkProvider implements NetworkProvider<WebSocketServerConfig> {
    @Nonnull @Override public String getId() { return "ws-server"; }
    @Nonnull @Override public NetworkType getType() { return DefaultNetworkType.WEB_SOCKET_SERVER; }
    @Nonnull @Override public Mono<WebSocketServerConfig> createConfig(@Nonnull NetworkProperties properties) {
        WebSocketServerConfig cfg = new WebSocketServerConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }
    @Nonnull @Override public Mono<Network> createNetwork(@Nonnull WebSocketServerConfig config) { return Mono.just(new VertxWebSocketServerNetwork()); }
    @Override public Mono<Network> reload(@Nonnull Network network, @Nonnull WebSocketServerConfig config) { return Mono.just(network); }
}
