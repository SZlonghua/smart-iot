package net.lab1024.sa.base.module.support.protocol.http.network;
import javax.annotation.Nonnull;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;
public class HttpClientNetworkProvider implements NetworkProvider<HttpClientConfig> {
    @Nonnull @Override public String getId() { return "http-client"; }
    @Nonnull @Override public NetworkType getType() { return DefaultNetworkType.HTTP_CLIENT; }
    @Nonnull @Override public Mono<HttpClientConfig> createConfig(@Nonnull NetworkProperties properties) {
        HttpClientConfig cfg = new HttpClientConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }
    @Nonnull @Override public Mono<Network> createNetwork(@Nonnull HttpClientConfig config) { return Mono.just(new VertxHttpClientNetwork()); }
    @Override public Mono<Network> reload(@Nonnull Network network, @Nonnull HttpClientConfig config) { return Mono.just(network); }
}
