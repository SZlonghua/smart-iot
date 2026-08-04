package net.lab1024.sa.base.module.support.protocol.coap.network;
import javax.annotation.Nonnull;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;
public class CoapServerNetworkProvider implements NetworkProvider<CoapServerConfig> {
    @Nonnull @Override public String getId() { return "coap-server"; }
    @Nonnull @Override public NetworkType getType() { return DefaultNetworkType.COAP_SERVER; }
    @Nonnull @Override public Mono<CoapServerConfig> createConfig(@Nonnull NetworkProperties properties) {
        CoapServerConfig cfg = new CoapServerConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }
    @Nonnull @Override public Mono<Network> createNetwork(@Nonnull CoapServerConfig config) { return Mono.just(new VertxCoapServerNetwork()); }
    @Override public Mono<Network> reload(@Nonnull Network network, @Nonnull CoapServerConfig config) { return Mono.just(network); }
}
