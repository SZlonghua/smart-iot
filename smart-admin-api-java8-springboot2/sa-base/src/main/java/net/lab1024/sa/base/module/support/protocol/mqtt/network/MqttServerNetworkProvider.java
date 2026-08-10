package net.lab1024.sa.base.module.support.protocol.mqtt.network;

import cn.hutool.core.bean.BeanUtil;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.network.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * MQTT Server Network Provider。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
@Slf4j
public class MqttServerNetworkProvider implements NetworkProvider<MqttServerConfig> {

    private final Vertx vertx;

    public MqttServerNetworkProvider(Vertx vertx) {
        this.vertx = vertx;
    }

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
        BeanUtil.copyProperties(properties.getConfigurations(), cfg);
        cfg.setId(properties.getId());
        return Mono.just(cfg);
    }

    @Nonnull
    @Override
    public Mono<Network> createNetwork(@Nonnull MqttServerConfig config) {
        return Mono.just(new VertxMqttServerNetwork(config, vertx));
    }

    @Override
    public Mono<Network> reload(@Nonnull Network network, @Nonnull MqttServerConfig config) {
        VertxMqttServerNetwork mqttNetwork = (VertxMqttServerNetwork) network;
        mqttNetwork.reload(config);
        return Mono.just(network);
    }
}
