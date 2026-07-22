package net.lab1024.sa.base.module.support.eventbus.config;

import net.lab1024.sa.base.module.support.eventbus.core.IEventBus;
import net.lab1024.sa.base.module.support.eventbus.impl.SpringEventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;

/**
 * EventBus 自动装配。
 *
 * @Author 廖涛
 * @Date 2026/07/22
 * @Copyright 1024创新实验室
 */
@Configuration
public class EventBusAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private AsyncTaskExecutor asyncExecutor;

    @Bean
    public IEventBus eventBus() {
        return new SpringEventBus(applicationContext, asyncExecutor);
    }
}
