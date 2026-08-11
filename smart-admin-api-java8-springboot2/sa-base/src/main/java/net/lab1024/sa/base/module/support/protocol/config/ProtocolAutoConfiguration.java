package net.lab1024.sa.base.module.support.protocol.config;

import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDataService;
import net.lab1024.sa.base.common.protocol.ProtocolSupportLoader;
import net.lab1024.sa.base.common.protocol.ProtocolSupportLoaders;
import net.lab1024.sa.base.common.protocol.ProtocolSupportManager;
import net.lab1024.sa.base.common.spi.ServiceContext;
import net.lab1024.sa.base.common.spi.SpringServiceContext;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import net.lab1024.sa.base.module.support.protocol.DefaultProtocolSupportLoaders;
import net.lab1024.sa.base.module.support.protocol.DefaultProtocolSupportManager;
import net.lab1024.sa.base.module.support.protocol.JarProtocolSupportLoader;
import net.lab1024.sa.base.module.support.protocol.LocalProtocolSupportLoader;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

import java.util.List;

/**
 * ProtocolSupport 模块自动装配。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/07/27
 * &#064;Copyright  1024创新实验室
 */
@Configuration
public class ProtocolAutoConfiguration {
    @Bean
    public ServiceContext serviceContext(ApplicationContext applicationContext){
        return new SpringServiceContext(applicationContext);
    }

    @Bean(initMethod = "init")
    public ProtocolSupportManager protocolSupportManager(ProtocolSupportLoaders loaders,
                                                          IEventBus eventBus,
                                                          ObjectProvider<ProtocolSupport> customSupport,
                                                          ProtocolSupportDataService protocolSupportDataService) {
        return new DefaultProtocolSupportManager(loaders, eventBus, customSupport, protocolSupportDataService);
    }

    @Bean
    public ProtocolSupportLoaders protocolSupportLoaders(List<ProtocolSupportLoader> loaders) {
        return new DefaultProtocolSupportLoaders(loaders);
    }

    @Bean
    public ProtocolSupportLoader jarProtocolSupportLoader(ServiceContext serviceContext) {
        return new JarProtocolSupportLoader(serviceContext);
    }

    @Bean
    @Profile("dev")
    public ProtocolSupportLoader localProtocolSupportLoader(ServiceContext serviceContext) {
        return new LocalProtocolSupportLoader(serviceContext);
    }

    /** 服务端口就绪后再加载协议（jar 包可能托管在本服务） */
    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        event.getApplicationContext()
                .getBean(DefaultProtocolSupportManager.class)
                .loadAll()
                .subscribe();
    }
}
