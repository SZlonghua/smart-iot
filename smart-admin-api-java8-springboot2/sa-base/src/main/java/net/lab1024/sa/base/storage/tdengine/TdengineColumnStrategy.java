package net.lab1024.sa.base.storage.tdengine;

import net.lab1024.sa.base.storage.StoragePolicy;
import net.lab1024.sa.base.storage.timeseries.TimeseriesChannel;
import net.lab1024.sa.base.storage.timeseries.TimeseriesColumnStorageStrategy;

/**
 * TDengine 单值列存储策略（tdengine-column）— 属性表每属性一行（tags={device_id, property} +
 * 固定类型值列），事件/日志表与行策略同形态（行协议与 SQL 由家族基类生成）。
 * <p>
 * 布局与查询逻辑全部继承时序单值列布局策略族，本类为叶子：仅注入通道（与行策略共享同一
 * {@link TdengineChannel} bean）并声明策略 ID；行协议与 SQL 方言对 TDengine 即家族默认，无需覆写。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
public class TdengineColumnStrategy extends TimeseriesColumnStorageStrategy {

    public TdengineColumnStrategy(TimeseriesChannel channel) {
        super(channel);
    }

    @Override
    public String getId() {
        return StoragePolicy.TDENGINE_COLUMN;
    }
}
