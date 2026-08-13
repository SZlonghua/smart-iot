package net.lab1024.sa.base.module.support.protocol.websocket.config;
import net.lab1024.sa.base.common.network.NetworkProvider;
import net.lab1024.sa.base.module.support.protocol.websocket.network.WebSocketClientConfig;
import net.lab1024.sa.base.module.support.protocol.websocket.network.WebSocketClientNetworkProvider;
import net.lab1024.sa.base.module.support.protocol.websocket.network.WebSocketServerConfig;
import net.lab1024.sa.base.module.support.protocol.websocket.network.WebSocketServerNetworkProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class WebSocketNetworkComponentConfig {
    @Bean public NetworkProvider<WebSocketServerConfig> wsServerNetworkProvider() { return new WebSocketServerNetworkProvider(); }
    @Bean public NetworkProvider<WebSocketClientConfig> wsClientNetworkProvider() { return new WebSocketClientNetworkProvider(); }
}
