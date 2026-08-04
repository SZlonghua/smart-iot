package net.lab1024.sa.base.module.support.protocol.tcp.network;
import javax.annotation.Nonnull;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;
public class TcpClientNetworkProvider implements NetworkProvider<TcpClientConfig> {
    @Nonnull @Override public String getId() { return "tcp-client"; }
    @Nonnull @Override public NetworkType getType() { return DefaultNetworkType.TCP_CLIENT; }
    @Nonnull @Override public Mono<TcpClientConfig> createConfig(@Nonnull NetworkProperties properties) {
        TcpClientConfig cfg = new TcpClientConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }
    @Nonnull @Override public Mono<Network> createNetwork(@Nonnull TcpClientConfig config) { return Mono.just(new VertxTcpClientNetwork()); }
    @Override public Mono<Network> reload(@Nonnull Network network, @Nonnull TcpClientConfig config) { return Mono.just(network); }
}
