package net.lab1024.sa.base.common.gateway.config;

import net.lab1024.sa.base.common.gateway.*;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import net.lab1024.sa.base.module.support.protocol.children.gateway.ChildrenDeviceGatewayProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * DeviceGateway 模块自动装配。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
@Configuration
public class DeviceGatewayAutoConfiguration {

    @Bean(initMethod = "init")
    public DeviceGatewayManager deviceGatewayManager(DeviceGatewayProviders providers,
                                                      DeviceGatewayPropertiesManager deviceGatewayPropertiesManager,
                                                      IEventBus eventBus) {
        return new DefaultDeviceGatewayManager(providers, deviceGatewayPropertiesManager, eventBus);
    }

    @Bean
    public DeviceGatewayProviders deviceGatewayProviders(List<DeviceGatewayProvider> providerList) {
        return new DefaultDeviceGatewayProviders(providerList);
    }

    @Bean
    public DeviceGatewayProvider childrenDeviceGatewayProvider() {
        return new ChildrenDeviceGatewayProvider();
    }

    /**
     * 网关加载 — @Order(2) 晚于协议加载（ProtocolAutoConfiguration @Order(1)），
     * 保证网关创建时协议已注册完成。
     */
    @Order(20)
    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        event.getApplicationContext()
                .getBean(DeviceGatewayManager.class)
                .loadAll()
                .subscribe();
    }
}
