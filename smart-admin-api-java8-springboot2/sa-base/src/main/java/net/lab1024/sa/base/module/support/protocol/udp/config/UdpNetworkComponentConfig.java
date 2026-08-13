package net.lab1024.sa.base.module.support.protocol.udp.config;
import net.lab1024.sa.base.common.network.NetworkProvider;
import net.lab1024.sa.base.module.support.protocol.udp.network.UdpConfig;
import net.lab1024.sa.base.module.support.protocol.udp.network.UdpNetworkProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class UdpNetworkComponentConfig {
    @Bean public NetworkProvider<UdpConfig> udpNetworkProvider() { return new UdpNetworkProvider(); }
}
