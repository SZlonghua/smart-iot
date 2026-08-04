package net.lab1024.sa.base.module.support.protocol.mqtt.config;

import net.lab1024.sa.base.common.network.NetworkProvider;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttClientConfig;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttClientNetworkProvider;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttServerConfig;
import net.lab1024.sa.base.module.support.protocol.mqtt.network.MqttServerNetworkProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttNetworkComponentConfig {
    @Bean
    public NetworkProvider<MqttServerConfig> mqttServerNetworkProvider() {
        return new MqttServerNetworkProvider();
    }

    @Bean
    public NetworkProvider<MqttClientConfig> mqttClientNetworkProvider() {
        return new MqttClientNetworkProvider();
    }
}
