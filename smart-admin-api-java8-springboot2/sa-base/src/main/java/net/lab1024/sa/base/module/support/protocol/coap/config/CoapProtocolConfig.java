package net.lab1024.sa.base.module.support.protocol.coap.config;

import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.module.support.protocol.coap.gateway.CoapDeviceGatewayProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoapProtocolConfig {

    @Bean
    public DeviceGatewayProvider coapDeviceGatewayProvider() {
        return new CoapDeviceGatewayProvider();
    }
}
