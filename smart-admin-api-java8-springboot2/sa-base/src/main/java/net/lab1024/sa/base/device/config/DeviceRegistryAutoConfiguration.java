package net.lab1024.sa.base.device.config;

import net.lab1024.sa.base.device.DeviceRegistry;
import net.lab1024.sa.base.device.support.DefaultDeviceRegistry;
import net.lab1024.sa.base.module.support.cache.core.ICacheManager;
import net.lab1024.sa.base.module.support.cache.core.IConfigStorageManager;
import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * DeviceRegistry 自动装配。
 *
 * @Author 廖涛
 * @Date 2026/07/25
 * @Copyright 1024创新实验室
 */
@Configuration
public class DeviceRegistryAutoConfiguration {

    @Bean
    public DeviceRegistry deviceRegistry(IConfigStorageManager configStorageManager,
                                         @Qualifier("cacheOperationManager") ICacheManager cacheManager,
                                         IEventBus eventBus) {
        return new DefaultDeviceRegistry(configStorageManager, cacheManager, eventBus);
    }
}
