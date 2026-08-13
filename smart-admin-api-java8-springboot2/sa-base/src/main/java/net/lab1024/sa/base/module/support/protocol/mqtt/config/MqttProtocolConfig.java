package net.lab1024.sa.base.module.support.protocol.mqtt.config;

import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.common.message.DecodedClientMessageHandler;
import net.lab1024.sa.base.common.network.NetworkManager;
import net.lab1024.sa.base.common.protocol.ProtocolSupportManager;
import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.session.DeviceSessionManager;
import net.lab1024.sa.base.module.support.protocol.mqtt.gateway.MqttClientDeviceGatewayProvider;
import net.lab1024.sa.base.module.support.protocol.mqtt.gateway.MqttServerDeviceGatewayProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttProtocolConfig {

    @Bean
    public DeviceGatewayProvider mqttServerDeviceGatewayProvider(NetworkManager networkManager,
                                                                 DeviceRegistry registry,
                                                                 DeviceSessionManager sessionManager,
                                                                 DecodedClientMessageHandler messageHandler,
                                                                 ProtocolSupportManager protocolSupportManager) {
        return new MqttServerDeviceGatewayProvider(networkManager, registry, sessionManager, messageHandler, protocolSupportManager);
    }

    @Bean
    public DeviceGatewayProvider mqttClientDeviceGatewayProvider() {
        return new MqttClientDeviceGatewayProvider();
    }
}
