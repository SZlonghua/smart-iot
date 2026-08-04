package net.lab1024.sa.base.module.support.protocol.coap.network;
import javax.annotation.Nonnull;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;
public class CoapClientNetworkProvider implements NetworkProvider<CoapClientConfig> {
    @Nonnull @Override public String getId() { return "coap-client"; }
    @Nonnull @Override public NetworkType getType() { return DefaultNetworkType.COAP_CLIENT; }
    @Nonnull @Override public Mono<CoapClientConfig> createConfig(@Nonnull NetworkProperties properties) {
        CoapClientConfig cfg = new CoapClientConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }
    @Nonnull @Override public Mono<Network> createNetwork(@Nonnull CoapClientConfig config) { return Mono.just(new VertxCoapClientNetwork()); }
    @Override public Mono<Network> reload(@Nonnull Network network, @Nonnull CoapClientConfig config) { return Mono.just(network); }
}
