package net.lab1024.sa.base.storage;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储策略注册器 — 构造时收集容器内全部 {@link StorageStrategy} 策略 bean，供设备操作对象按产品
 * 存储策略 ID 查找；新增策略（ES / ClickHouse / InfluxDB 等）只需注册策略 bean，本类零改动。
 * <p>
 * 注册器无条件装配、恒持有 {@link EmptyStorageStrategy} 兜底：{@link #get(String)} 永不返回 null ——
 * 策略已注册返回真实实现，存储未开启/无匹配策略返回 EmptyStorageStrategy
 * （写入仅告警一次、查询明确报错），调用方无需判空。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Slf4j
public class StorageStrategyRegistry {

    private final Map<String, StorageStrategy> strategyMap = new ConcurrentHashMap<>();

    /** 兜底空策略 — 存储未开启或无匹配策略时返回（写入静默告警一次、查询抛错） */
    private final StorageStrategy emptyStrategy = new EmptyStorageStrategy();

    public StorageStrategyRegistry(List<StorageStrategy> strategyList) {
        for (StorageStrategy strategy : strategyList) {
            if (strategy.getId() == null) {
                log.warn("存储策略 [{}] 未声明策略 ID，跳过注册", strategy.getClass().getSimpleName());
                continue;
            }
            StorageStrategy previous = strategyMap.put(strategy.getId(), strategy);
            if (previous != null) {
                log.warn("存储策略 ID [{}] 重复注册，后者覆盖前者: {} -> {}", strategy.getId(), previous.getClass(), strategy.getClass());
            }
        }
        if (strategyMap.isEmpty()) {
            log.info("设备消息数据存储未启用（iot.storage 未开启或无策略 bean），消息写入与历史查询将走 EmptyStorageStrategy");
        }
    }

    /** 按策略 ID 查找 — 永不返回 null（未注册/存储未开启返回 EmptyStorageStrategy 兜底） */
    public StorageStrategy get(String id) {
        if (id == null) {
            return emptyStrategy;
        }
        return strategyMap.getOrDefault(id, emptyStrategy);
    }
}
