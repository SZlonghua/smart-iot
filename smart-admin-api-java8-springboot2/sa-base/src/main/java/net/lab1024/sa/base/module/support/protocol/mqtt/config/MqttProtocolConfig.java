package net.lab1024.sa.base.module.support.protocol.mqtt.config;

import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.module.support.protocol.mqtt.gateway.MqttClientDeviceGatewayProvider;
import net.lab1024.sa.base.module.support.protocol.mqtt.gateway.MqttServerDeviceGatewayProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttProtocolConfig {

    @Bean
    public DeviceGatewayProvider mqttServerDeviceGatewayProvider() {
        return new MqttServerDeviceGatewayProvider();
    }

    @Bean
    public DeviceGatewayProvider mqttClientDeviceGatewayProvider() {
        return new MqttClientDeviceGatewayProvider();
    }
}
