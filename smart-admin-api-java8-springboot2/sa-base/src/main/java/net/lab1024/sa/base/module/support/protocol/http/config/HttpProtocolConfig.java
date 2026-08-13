package net.lab1024.sa.base.module.support.protocol.http.config;

import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.module.support.protocol.http.gateway.HttpServerDeviceGatewayProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpProtocolConfig {

    @Bean
    public DeviceGatewayProvider httpServerDeviceGatewayProvider() {
        return new HttpServerDeviceGatewayProvider();
    }
}
