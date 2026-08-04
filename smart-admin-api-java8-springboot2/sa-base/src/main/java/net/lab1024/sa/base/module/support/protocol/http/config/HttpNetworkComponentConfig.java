package net.lab1024.sa.base.module.support.protocol.http.config;
import net.lab1024.sa.base.common.network.NetworkProvider;
import net.lab1024.sa.base.module.support.protocol.http.network.HttpClientConfig;
import net.lab1024.sa.base.module.support.protocol.http.network.HttpClientNetworkProvider;
import net.lab1024.sa.base.module.support.protocol.http.network.HttpServerConfig;
import net.lab1024.sa.base.module.support.protocol.http.network.HttpServerNetworkProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class HttpNetworkComponentConfig {
    @Bean public NetworkProvider<HttpServerConfig> httpServerNetworkProvider() { return new HttpServerNetworkProvider(); }
    @Bean public NetworkProvider<HttpClientConfig> httpClientNetworkProvider() { return new HttpClientNetworkProvider(); }
}
