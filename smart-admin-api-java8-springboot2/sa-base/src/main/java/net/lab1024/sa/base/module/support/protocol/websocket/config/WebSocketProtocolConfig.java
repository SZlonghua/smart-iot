package net.lab1024.sa.base.module.support.protocol.websocket.config;

import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.module.support.protocol.websocket.gateway.WebSocketDeviceGatewayProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketProtocolConfig {

    @Bean
    public DeviceGatewayProvider webSocketDeviceGatewayProvider() {
        return new WebSocketDeviceGatewayProvider();
    }
}
