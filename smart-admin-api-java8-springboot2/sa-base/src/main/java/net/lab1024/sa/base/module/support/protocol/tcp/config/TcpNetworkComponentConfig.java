package net.lab1024.sa.base.module.support.protocol.tcp.config;

import net.lab1024.sa.base.common.network.NetworkProvider;
import net.lab1024.sa.base.module.support.protocol.tcp.network.TcpClientConfig;
import net.lab1024.sa.base.module.support.protocol.tcp.network.TcpClientNetworkProvider;
import net.lab1024.sa.base.module.support.protocol.tcp.network.TcpServerConfig;
import net.lab1024.sa.base.module.support.protocol.tcp.network.TcpServerNetworkProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcpNetworkComponentConfig {
    @Bean
    public NetworkProvider<TcpServerConfig> tcpServerNetworkProvider() {
        return new TcpServerNetworkProvider();
    }

    @Bean
    public NetworkProvider<TcpClientConfig> tcpClientNetworkProvider() {
        return new TcpClientNetworkProvider();
    }
}
