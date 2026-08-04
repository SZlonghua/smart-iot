package net.lab1024.sa.base.module.support.protocol.mqtt.network;

import javax.annotation.Nonnull;

import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;

public class MqttServerNetworkProvider implements NetworkProvider<MqttServerConfig> {
    @Nonnull
    @Override
    public String getId() {
        return "mqtt-server";
    }

    @Nonnull
    @Override
    public NetworkType getType() {
        return DefaultNetworkType.MQTT_SERVER;
    }

    @Nonnull
    @Override
    public Mono<MqttServerConfig> createConfig(@Nonnull NetworkProperties properties) {
        MqttServerConfig cfg = new MqttServerConfig();
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
    public Mono<Network> createNetwork(@Nonnull MqttServerConfig config) {
        return Mono.just(new VertxMqttServerNetwork());
    }

    @Override
    public Mono<Network> reload(@Nonnull Network network, @Nonnull MqttServerConfig config) {
        return Mono.just(network);
    }
}
