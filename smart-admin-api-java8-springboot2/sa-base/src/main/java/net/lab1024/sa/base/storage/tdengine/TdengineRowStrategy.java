package net.lab1024.sa.base.storage.tdengine;

import net.lab1024.sa.base.storage.StoragePolicy;
import net.lab1024.sa.base.storage.timeseries.TimeseriesChannel;
import net.lab1024.sa.base.storage.timeseries.TimeseriesRowStorageStrategy;

/**
 * TDengine 行布局存储策略（tdengine-row）— 属性一条消息一行（属性摊平为列）、事件/日志一行一条消息。
 * <p>
 * 布局与查询逻辑全部继承时序行布局策略族，本类为叶子：仅注入通道（与单值列策略共享同一
 * {@link TdengineChannel} bean）并声明策略 ID；行协议与 SQL 方言对 TDengine 即家族默认，无需覆写。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
public class TdengineRowStrategy extends TimeseriesRowStorageStrategy {

    public TdengineRowStrategy(TimeseriesChannel channel) {
        super(channel);
    }

    @Override
    public String getId() {
        return StoragePolicy.TDENGINE_ROW;
    }
}
