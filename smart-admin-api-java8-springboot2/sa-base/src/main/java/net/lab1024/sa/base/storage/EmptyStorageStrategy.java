package net.lab1024.sa.base.storage;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.exception.BusinessException;
import net.lab1024.sa.base.storage.model.CommandLogQuery;
import net.lab1024.sa.base.storage.model.EventData;
import net.lab1024.sa.base.storage.model.EventHistoryQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyValue;
import net.lab1024.sa.base.storage.model.MessageLogData;
import net.lab1024.sa.base.storage.model.PropertyData;
import net.lab1024.sa.base.storage.model.PropertyHistoryQuery;
import net.lab1024.sa.base.storage.model.StorageData;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 空存储策略 — 存储未开启（iot.storage.enabled=false，默认）或无匹配策略时的注册器兜底实现。
 * <p>
 * 由 {@link StorageStrategyRegistry} 恒持有，{@code registry.get(id)} 永不返回 null、调用方不再判空：
 * 写入路径仅告警一次后静默（默认关闭是高频率常态，不刷屏、不阻断消息流）；
 * 查询路径抛 {@link BusinessException} 明确报错（历史数据查询需要真实存储，不做静默空结果）。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Slf4j
public class EmptyStorageStrategy implements StorageStrategy {

    /** 写入告警仅一次（静态跨注册器实例共享，防高频上报刷屏） */
    private static final AtomicBoolean SAVE_WARN_ONCE = new AtomicBoolean(false);

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void save(StorageData data) {
        if (SAVE_WARN_ONCE.compareAndSet(false, true)) {
            log.warn("设备消息数据存储未开启或无匹配存储策略，消息不落库（仅提示一次；如需落库请配置 iot.storage.*）");
        }
    }

    @Override
    public PageResult<PropertyData> queryPropertyHistory(PropertyHistoryQuery query) {
        throw readError();
    }

    @Override
    public Map<String, LatestPropertyValue> queryLatestPropertyValues(LatestPropertyQuery query) {
        throw readError();
    }

    @Override
    public PageResult<EventData> queryEventHistory(EventHistoryQuery query) {
        throw readError();
    }

    @Override
    public PageResult<MessageLogData> queryCommandLogHistory(CommandLogQuery query) {
        throw readError();
    }

    private static BusinessException readError() {
        return new BusinessException("设备消息数据存储未开启或无匹配存储策略，无法查询历史数据");
    }
}
