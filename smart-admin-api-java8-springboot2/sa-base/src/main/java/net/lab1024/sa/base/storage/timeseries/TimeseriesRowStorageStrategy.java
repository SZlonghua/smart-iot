package net.lab1024.sa.base.storage.timeseries;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.storage.model.LatestPropertyQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyValue;
import net.lab1024.sa.base.storage.model.PropertyData;
import net.lab1024.sa.base.storage.model.PropertyHistoryQuery;
import net.lab1024.sa.base.storage.model.TimeseriesRow;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 时序行布局策略抽象基类 — 属性表一条消息一行（属性摊平为动态列，列数随产品属性集增长），
 * 事件/日志表行形态由 {@link TimeseriesStorageStrategy} 实现。
 * <p>
 * 与单值列布局的差异只在此三处（写 / 历史查询 / 最新值查询）：
 * ①写 — fields = 保留列 ∪ 属性摊平列（值按 Java 类型落列，schemaless 自动建列）；
 * ②历史 — 属性为动态列，以 {property} IS NOT NULL 收窄行集（仅含该属性的消息行）后 count + 时间倒序分页；
 * ③最新值 — 每属性独立取该属性列 _ts 倒序首行（{property} IS NOT NULL 条件兜底）。
 * 行解码约定：查询行除保留键（device_id/message_id/create_time/device_name）与主时间列 _ts 外其余列即属性。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
public abstract class TimeseriesRowStorageStrategy extends TimeseriesStorageStrategy {

    protected TimeseriesRowStorageStrategy(TimeseriesChannel channel) {
        super(channel);
    }

    /** 属性落库 — row 行形态：tags={device_id}，fields=保留列 ∪ 属性摊平列（属性集整批灌入，保留列名/null 由 SmartPoint 自动防护） */
    @Override
    protected void saveProperty(PropertyData data) {
        writeLines(Collections.singletonList(SmartPoint.builder()
                .table(timeseriesTable(TimeseriesLayout.TABLE_PREFIX_PROPERTY, data.getProductId()))
                .tag(TimeseriesLayout.TAG_DEVICE_ID, data.getDeviceId())
                .field(TimeseriesLayout.FIELD_MESSAGE_ID, data.getMessageId())
                .field(TimeseriesLayout.FIELD_CREATE_TIME, data.getTimestamp())
                .field(TimeseriesLayout.FIELD_DEVICE_NAME, data.getDeviceName())
                .fields(data.getProperties())
                .build()
                .toLineProtocol()));
    }

    /**
     * 属性历史分页查询 — 属性为动态列，以 {property} IS NOT NULL 收窄行集（仅含该属性的消息行）
     * 后 count + 时间倒序分页，整行解码返回
     */
    @Override
    public PageResult<PropertyData> queryPropertyHistory(PropertyHistoryQuery query) {
        TimeseriesQuery tsQuery = TimeseriesQuery.from(timeseriesTable(TimeseriesLayout.TABLE_PREFIX_PROPERTY, query.getProductId()))
                .eq(TimeseriesLayout.TAG_DEVICE_ID, query.getDeviceId())
                .fromTime(query.getStartTime())
                .toTime(query.getEndTime())
                .isNotNull(query.getProperty())
                .limit(query.getPageSize())
                .offset((long) (query.getPageNum() - 1) * query.getPageSize());
        return queryPage(tsQuery, TimeseriesRowStorageStrategy::propertyRowToData);
    }

    /**
     * 批量最新属性值 — 每属性独立查该属性列 _ts 倒序首行；属性从未上报（列/表不存在）通道返回空 → 无记录跳过
     */
    @Override
    public Map<String, LatestPropertyValue> queryLatestPropertyValues(LatestPropertyQuery query) {
        if (query.getPropertyIds() == null || query.getPropertyIds().isEmpty()) {
            return new LinkedHashMap<>();
        }

        String table = timeseriesTable(TimeseriesLayout.TABLE_PREFIX_PROPERTY, query.getProductId());

        Map<String, LatestPropertyValue> result = new LinkedHashMap<>();
        for (String propertyId : query.getPropertyIds()) {
            List<Map<String, Object>> rows = channel.executeQuery(latestValueSql(table, query.getDeviceId(), propertyId));
            if (rows.isEmpty()) {
                continue;
            }
            TimeseriesRow row = new TimeseriesRow(rows.get(0));
            Object value = row.get(TimeseriesLayout.rowKey(propertyId));
            result.put(propertyId, new LatestPropertyValue(value, row.getLong(TimeseriesLayout.COLUMN_TS)));
        }
        return result;
    }

    /** 属性列最新值 SQL 方言（属性列最新一行；_ts 由构建器恒追加；叶子策略可覆写方言） */
    protected String latestValueSql(String table, String deviceId, String property) {
        return TimeseriesQuery.from(table)
                .eq(TimeseriesLayout.TAG_DEVICE_ID, deviceId)
                .isNotNull(property)
                .columns(property)
                .firstSql();
    }

    /** row 形态查询行 → PropertyData（除保留键与主时间列 _ts 外的其余列为属性；时间为消息上报时间取 create_time） */
    private static PropertyData propertyRowToData(TimeseriesRow row) {
        PropertyData data = new PropertyData();
        data.setDeviceId(row.getString(TimeseriesLayout.TAG_DEVICE_ID));
        data.setMessageId(row.getString(TimeseriesLayout.FIELD_MESSAGE_ID));
        data.setDeviceName(row.getString(TimeseriesLayout.FIELD_DEVICE_NAME));
        data.setTimestamp(row.getLong(TimeseriesLayout.FIELD_CREATE_TIME));
        Map<String, Object> properties = new HashMap<>(row.asMap());
        properties.remove(TimeseriesLayout.TAG_DEVICE_ID);
        properties.remove(TimeseriesLayout.FIELD_MESSAGE_ID);
        properties.remove(TimeseriesLayout.FIELD_CREATE_TIME);
        properties.remove(TimeseriesLayout.FIELD_DEVICE_NAME);
        properties.remove(TimeseriesLayout.COLUMN_TS);
        data.setProperties(properties);
        return data;
    }

}
