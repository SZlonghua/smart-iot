package net.lab1024.sa.base.module.support.protocol.tcp.config;

import net.lab1024.sa.base.common.gateway.DeviceGatewayProvider;
import net.lab1024.sa.base.module.support.protocol.tcp.gateway.TcpDeviceGatewayProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcpProtocolConfig {

    @Bean
    public DeviceGatewayProvider tcpDeviceGatewayProvider() {
        return new TcpDeviceGatewayProvider();
    }
}
