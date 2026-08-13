package net.lab1024.sa.base.module.support.protocol.udp.network;
import javax.annotation.Nonnull;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;
public class UdpNetworkProvider implements NetworkProvider<UdpConfig> {
    @Nonnull @Override public String getId() { return "udp"; }
    @Nonnull @Override public NetworkType getType() { return DefaultNetworkType.UDP; }
    @Nonnull @Override public Mono<UdpConfig> createConfig(@Nonnull NetworkProperties properties) {
        UdpConfig cfg = new UdpConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }
    @Nonnull @Override public Mono<Network> createNetwork(@Nonnull UdpConfig config) { return Mono.just(new VertxUdpNetwork()); }
    @Override public Mono<Network> reload(@Nonnull Network network, @Nonnull UdpConfig config) { return Mono.just(network); }
}
