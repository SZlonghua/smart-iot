package net.lab1024.sa.base.module.support.protocol.mqtt.network;
import javax.annotation.Nonnull;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;
public class MqttClientNetworkProvider implements NetworkProvider<MqttClientConfig> {
    @Nonnull @Override public String getId() { return "mqtt-client"; }
    @Nonnull @Override public NetworkType getType() { return DefaultNetworkType.MQTT_CLIENT; }
    @Nonnull @Override public Mono<MqttClientConfig> createConfig(@Nonnull NetworkProperties properties) {
        MqttClientConfig cfg = new MqttClientConfig();
        cfg.setId(properties.getId());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> vals = properties.getConfigurations();
        if (vals != null) {
            if (vals.containsKey("host")) cfg.setHost((String) vals.get("host"));
            if (vals.containsKey("port")) cfg.setPort(Integer.parseInt(String.valueOf(vals.get("port"))));
        }
        return Mono.just(cfg);
    }
    @Nonnull @Override public Mono<Network> createNetwork(@Nonnull MqttClientConfig config) { return Mono.just(new VertxMqttClientNetwork()); }
    @Override public Mono<Network> reload(@Nonnull Network network, @Nonnull MqttClientConfig config) { return Mono.just(network); }
}
