package net.lab1024.sa.base.module.support.protocol.mqtt.network;

import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttServerOptions;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.network.DefaultNetworkType;
import net.lab1024.sa.base.common.network.NetworkType;
import net.lab1024.sa.base.module.support.protocol.mqtt.session.VertxMqttConnection;

import java.util.function.Consumer;

/**
 * Vertx MQTT Server Network 实现。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
@Slf4j
public class VertxMqttServerNetwork implements MqttServerNetwork {

    private final String id;
    @Setter
    private MqttServerConfig config;
    private final Vertx vertx;
    private volatile MqttServer mqttServer;
    private volatile Consumer<VertxMqttConnection> connectionListener;

    public VertxMqttServerNetwork(MqttServerConfig config, Vertx vertx) {
        this.id = config.getId();
        this.config = config;
        this.vertx = vertx;
        this.mqttServer = createMqttServer(config);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public NetworkType getType() {
        return DefaultNetworkType.MQTT_SERVER;
    }

    @Override
    public void start() {
        mqttServer.endpointHandler(endpoint -> {
            if (connectionListener != null) {
                connectionListener.accept(new VertxMqttConnection(endpoint));
            }
        }).listen(ar -> {
            if (ar.succeeded()) {
                log.info("MQTT server started on {},network id {}", config.getLocalPort(), config.getId());
            } else {
                log.error("MQTT server start failed {},network id {}", ar.cause().getMessage(), config.getId());
            }
        });
    }

    @Override
    public void shutdown() {
        if (mqttServer != null) {
            mqttServer.close(res -> {
                if (res.failed()) {
                    log.error(res.cause().getMessage(), res.cause());
                } else {
                    log.debug("mqtt server [{}] closed", mqttServer.actualPort());
                }
            });
            mqttServer = null;
        }
    }

    @Override
    public boolean isAlive() {
        return mqttServer != null;
    }

    @Override
    public boolean isAutoReload() {
        return true;
    }

    /** 内部 reload — 关闭旧 MqttServer 并更新 config，外部会重新 start() */
    public void reload(MqttServerConfig newConfig) {
        shutdown();
        this.config = newConfig;
        this.mqttServer = createMqttServer(newConfig);
    }

    @Override
    public void onConnection(Consumer<VertxMqttConnection> listener) {
        this.connectionListener = listener;
    }

    private MqttServer createMqttServer(MqttServerConfig config) {
        MqttServerOptions options = new MqttServerOptions()
                .setHost(config.getLocalAddress())
                .setPort(config.getLocalPort())
                .setMaxMessageSize(config.getMaxMessageSize());
        return MqttServer.create(vertx, options);
    }
}
