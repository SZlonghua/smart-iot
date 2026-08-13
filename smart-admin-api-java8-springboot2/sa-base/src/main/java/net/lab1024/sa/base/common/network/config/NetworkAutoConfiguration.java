package net.lab1024.sa.base.common.network.config;

import net.lab1024.sa.base.common.network.*;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NetworkAutoConfiguration {

    @Bean(initMethod = "init")
    public DefaultNetworkManager networkManager(NetworkProviders<? extends NetworkConfig> providers,
                                                  NetworkConfigManager networkConfigManager,
                                                  IEventBus eventBus) {
        return new DefaultNetworkManager(providers, networkConfigManager, eventBus);
    }

    @Bean
    public <C extends NetworkConfig> NetworkProviders<C> networkProviders(List<NetworkProvider<C>> providerList) {
        return new DefaultNetworkProviders<C>(providerList);
    }
}
