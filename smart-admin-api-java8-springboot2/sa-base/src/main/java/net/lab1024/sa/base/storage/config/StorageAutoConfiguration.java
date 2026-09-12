package net.lab1024.sa.base.storage.config;

import net.lab1024.sa.base.storage.StorageStrategy;
import net.lab1024.sa.base.storage.StorageStrategyRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 设备消息存储通用装配 — 注册器无条件装配（存储未开启时仅注册器 + 兜底 EmptyStorageStrategy 存在，
 * 策略类全部不装配，监听器/服务调用走空策略静默/报错，应用照常运行）。
 * <p>
 * List 注入收集容器内全部 StorageStrategy 策略 bean：策略自身的开启条件由各自 AutoConfiguration 负责
 * （tdengine 策略依赖 iot.storage.enabled + iot.storage.tdengine.enabled），此处零改动即可接入新策略
 * （ES / ClickHouse / InfluxDB 等只需注册策略 bean）。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Configuration
public class StorageAutoConfiguration {

    /** 存储策略注册器 — 恒装配（含 EmptyStorageStrategy 兜底，get 永不返回 null） */
    @Bean
    public StorageStrategyRegistry storageStrategyRegistry(List<StorageStrategy> strategyList) {
        return new StorageStrategyRegistry(strategyList);
    }
}
