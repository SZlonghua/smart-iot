package net.lab1024.sa.base.module.support.protocol.coap.config;
import net.lab1024.sa.base.common.network.NetworkProvider;
import net.lab1024.sa.base.module.support.protocol.coap.network.CoapClientConfig;
import net.lab1024.sa.base.module.support.protocol.coap.network.CoapClientNetworkProvider;
import net.lab1024.sa.base.module.support.protocol.coap.network.CoapServerConfig;
import net.lab1024.sa.base.module.support.protocol.coap.network.CoapServerNetworkProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class CoapNetworkComponentConfig {
    @Bean public NetworkProvider<CoapServerConfig> coapServerNetworkProvider() { return new CoapServerNetworkProvider(); }
    @Bean public NetworkProvider<CoapClientConfig> coapClientNetworkProvider() { return new CoapClientNetworkProvider(); }
}
