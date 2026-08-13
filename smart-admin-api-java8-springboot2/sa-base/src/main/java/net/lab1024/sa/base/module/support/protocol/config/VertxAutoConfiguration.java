package net.lab1024.sa.base.module.support.protocol.config;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Vertx 实例配置 — 所有协议公用。
 *
 * @Author 廖涛
 * @Date 2026/08/07
 * @Copyright 1024创新实验室
 */
@Configuration
public class VertxAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "vertx")
    public VertxOptions vertxOptions() {
        return new VertxOptions();
    }

    @Bean(destroyMethod = "close")
    public Vertx vertx(VertxOptions vertxOptions) {
        return Vertx.vertx(vertxOptions);
    }
}
