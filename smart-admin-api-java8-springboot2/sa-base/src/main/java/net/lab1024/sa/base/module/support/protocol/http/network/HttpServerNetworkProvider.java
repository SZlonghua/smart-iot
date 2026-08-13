package net.lab1024.sa.base.module.support.protocol.http.network;
import javax.annotation.Nonnull;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;
public class HttpServerNetworkProvider implements NetworkProvider<HttpServerConfig> {
    @Nonnull @Override public String getId() { return "http-server"; }
    @Nonnull @Override public NetworkType getType() { return DefaultNetworkType.HTTP_SERVER; }
    @Nonnull @Override public Mono<HttpServerConfig> createConfig(@Nonnull NetworkProperties properties) {
        HttpServerConfig cfg = new HttpServerConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }
    @Nonnull @Override public Mono<Network> createNetwork(@Nonnull HttpServerConfig config) { return Mono.just(new VertxHttpServerNetwork()); }
    @Override public Mono<Network> reload(@Nonnull Network network, @Nonnull HttpServerConfig config) { return Mono.just(network); }
}
