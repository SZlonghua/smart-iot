package net.lab1024.sa.base.storage.timeseries;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.storage.model.LatestPropertyQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyValue;
import net.lab1024.sa.base.storage.model.PropertyData;
import net.lab1024.sa.base.storage.model.PropertyHistoryQuery;
import net.lab1024.sa.base.storage.model.TimeseriesRow;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 时序单值列布局策略抽象基类 — 属性表每属性值一行（列数不随产品属性集膨胀，规避动态列表列数上限），
 * 事件/日志表行形态由 {@link TimeseriesStorageStrategy} 实现。
 * <p>
 * 与行布局的差异只在属性表（写 / 历史查询 / 最新值查询）：
 * ①写 — 一条上报消息的属性拆为多行（tags={device_id, property}），共享保留列
 * （messageId/create_time/deviceName，行间按 messageId 关联还原一次上报），值落单值类型列
 * （long_value/string_value/double_value/date_value/boolean_value 按 Java 类型选一，见
 * {@link TimeseriesLayout#valueColumnName}）；
 * ②历史 — property 为 tag 等值条件进 SQL 收窄，count + 时间倒序分页（一行 = 一个属性值）；
 * ③最新值 — 值列类型未知 → 5 类型值列 + _ts 全取最后一行，取首个非空值列即属性值
 * （{@link TimeseriesLayout#COLUMN_VALUE_KEYS} 扫描序）。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
public abstract class TimeseriesColumnStorageStrategy extends TimeseriesStorageStrategy {

    protected TimeseriesColumnStorageStrategy(TimeseriesChannel channel) {
        super(channel);
    }

    /** 属性落库 — column 行形态：row 形态一行（一条消息）按属性拆为单值行，行间共享保留列 */
    @Override
    protected void saveProperty(PropertyData data) {
        Map<String, Object> properties = data.getProperties();
        if (properties == null || properties.isEmpty()) {
            return;
        }

        String table = timeseriesTable(TimeseriesLayout.TABLE_PREFIX_PROPERTY, data.getProductId());

        List<String> lines = new ArrayList<>(properties.size());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String property = entry.getKey();
            Object value = entry.getValue();
            if (property == null || TimeseriesLayout.isReservedField(property) || value == null) {
                continue;
            }
            lines.add(getLineProtocol(data, table, property, value));
        }

        writeLines(lines);
    }

    private static @NotNull String getLineProtocol(PropertyData data, String table, String property, Object value) {
        return SmartPoint.builder()
                .table(table)
                .tag(TimeseriesLayout.TAG_DEVICE_ID, data.getDeviceId())
                .tag(TimeseriesLayout.TAG_PROPERTY, property)
                .field(TimeseriesLayout.FIELD_MESSAGE_ID, data.getMessageId())
                .field(TimeseriesLayout.FIELD_CREATE_TIME, data.getTimestamp())
                .field(TimeseriesLayout.FIELD_DEVICE_NAME, data.getDeviceName())
                .field(TimeseriesLayout.valueColumnName(value), value)
                .build()
                .toLineProtocol();
    }

    /**
     * 属性历史分页查询 — property 为 tag 等值条件进 SQL 收窄后 count + 时间倒序分页；
     * 行解码为一个属性值一行
     */
    @Override
    public PageResult<PropertyData> queryPropertyHistory(PropertyHistoryQuery query) {
        TimeseriesQuery tsQuery = TimeseriesQuery.from(timeseriesTable(TimeseriesLayout.TABLE_PREFIX_PROPERTY, query.getProductId()))
                .eq(TimeseriesLayout.TAG_DEVICE_ID, query.getDeviceId())
                .fromTime(query.getStartTime())
                .toTime(query.getEndTime())
                .eq(TimeseriesLayout.TAG_PROPERTY, query.getProperty())
                .limit(query.getPageSize())
                .offset((long) (query.getPageNum() - 1) * query.getPageSize());
        return queryPage(tsQuery, TimeseriesColumnStorageStrategy::columnPropertyRowToData);
    }

    /**
     * 批量最新属性值 — 每属性独立查单子表（deviceId+property 双 tag 命中）最后一行；
     * 属性从未上报（表不存在/子表无记录）通道返回空 → 无记录跳过
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
            Object value = columnRowValue(row);
            if (value != null) {
                result.put(propertyId, new LatestPropertyValue(value, row.getLong(TimeseriesLayout.COLUMN_TS)));
            }
        }
        return result;
    }

    /** 单属性最新值 SQL 方言 — 值列类型未知 → 5 类型值列全取（_ts 由构建器恒追加；叶子策略可覆写方言） */
    protected String latestValueSql(String table, String deviceId, String property) {
        return TimeseriesQuery.from(table)
                .eq(TimeseriesLayout.TAG_DEVICE_ID, deviceId)
                .eq(TimeseriesLayout.TAG_PROPERTY, property)
                .columns(TimeseriesLayout.COLUMN_VALUE_KEYS)
                .firstSql();
    }

    /** column 形态查询行 → PropertyData（property tag 为属性名，单值类型列首个非空为该行属性值；时间为消息上报时间取 create_time） */
    private static PropertyData columnPropertyRowToData(TimeseriesRow row) {
        PropertyData data = new PropertyData();
        data.setDeviceId(row.getString(TimeseriesLayout.TAG_DEVICE_ID));
        data.setMessageId(row.getString(TimeseriesLayout.FIELD_MESSAGE_ID));
        data.setDeviceName(row.getString(TimeseriesLayout.FIELD_DEVICE_NAME));
        data.setTimestamp(row.getLong(TimeseriesLayout.FIELD_CREATE_TIME));

        Map<String, Object> properties = new HashMap<>(2);
        String property = row.getString(TimeseriesLayout.TAG_PROPERTY);
        if (property != null) {
            properties.put(property, columnRowValue(row));
        }

        data.setProperties(properties);
        return data;
    }

    /** column 形态单值列的该行属性值 — 5 类型值列按序取首个非空（物理行恰有一个值列落值，其余为 null） */
    private static Object columnRowValue(TimeseriesRow row) {
        for (String valueKey : TimeseriesLayout.COLUMN_VALUE_KEYS) {
            Object value = row.get(valueKey);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
