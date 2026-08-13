package net.lab1024.sa.base.module.support.protocol.mqtt.network;

import net.lab1024.sa.base.common.network.ServerNetwork;
import net.lab1024.sa.base.module.support.protocol.mqtt.session.VertxMqttConnection;

import java.util.function.Consumer;

/**
 * MQTT Server Network。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
public interface MqttServerNetwork extends ServerNetwork {
    void onConnection(Consumer<VertxMqttConnection> listener);
}
