package net.lab1024.sa.base.module.support.protocol.mqtt.config;

import io.vertx.core.Vertx;
import net.lab1024.sa.base.common.network.NetworkProvider;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttClientConfig;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttClientNetworkProvider;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttServerConfig;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttServerNetworkProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT 网络组件配置。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
@Configuration
public class MqttNetworkComponentConfig {

    @Bean
    public NetworkProvider<MqttServerConfig> mqttServerNetworkProvider(Vertx vertx) {
        return new MqttServerNetworkProvider(vertx);
    }

    @Bean
    public NetworkProvider<MqttClientConfig> mqttClientNetworkProvider() {
        return new MqttClientNetworkProvider();
    }
}
