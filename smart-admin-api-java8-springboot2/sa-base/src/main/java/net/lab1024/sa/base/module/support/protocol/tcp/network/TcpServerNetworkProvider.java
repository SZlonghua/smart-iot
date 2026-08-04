package net.lab1024.sa.base.module.support.protocol.tcp.network;

import javax.annotation.Nonnull;

import net.lab1024.sa.base.common.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class TcpServerNetworkProvider implements NetworkProvider<TcpServerConfig> {
    private static final Logger log = LoggerFactory.getLogger(TcpServerNetworkProvider.class);

    @Nonnull
    @Override
    public String getId() {
        return "tcp-server";
    }

    @Nonnull
    @Override
    public NetworkType getType() {
        return DefaultNetworkType.TCP_SERVER;
    }

    @Nonnull
    @Override
    public Mono<TcpServerConfig> createConfig(@Nonnull NetworkProperties properties) {
        TcpServerConfig cfg = new TcpServerConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }

    @Nonnull
    @Override
    public Mono<Network> createNetwork(@Nonnull TcpServerConfig config) {
        log.info("--------------createNetwork--------------");
        return Mono.just(new VertxTcpServerNetwork());
    }

    @Override
    public Mono<Network> reload(@Nonnull Network network, @Nonnull TcpServerConfig config) {
        log.info("--------------reload--------------");
        return Mono.just(network);
    }
}
