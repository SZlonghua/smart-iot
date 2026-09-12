package net.lab1024.sa.base.storage.tdengine;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.storage.StoragePolicy;
import net.lab1024.sa.base.storage.model.LatestPropertyQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyValue;
import net.lab1024.sa.base.storage.model.PropertyData;
import net.lab1024.sa.base.storage.model.PropertyHistoryQuery;
import net.lab1024.sa.base.storage.timeseries.FakeTimeseriesChannel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDengine 单值列布局策略测试 — 属性一条消息拆为多单值行（tags={device_id, property} + 共享保留列 +
 * 5 类型列按 Java 类型落一列），消息时间落 create_time 列、落库时间由 TDengine 服务器写入主时间列 _ts（行协议不携带时间戳）；
 * 历史按 property tag 进 SQL 收窄、解码单值类型列首个非空为属性值；
 * 最新值每属性独立查子表。经 FakeTimeseriesChannel 断言精确行协议与 SQL。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
class TdengineColumnStrategyTest {

    private static final long TS = 1720000000000L;

    private final FakeTimeseriesChannel channel = new FakeTimeseriesChannel();
    private final TdengineColumnStrategy strategy = new TdengineColumnStrategy(channel);

    // ===== 写：一条消息按属性拆多行，每行恰落一个单值类型列 =====

    @Test
    void savePropertySplitsIntoColumnRows() {
        PropertyData data = columnPropertyData();
        strategy.save(data);

        assertEquals(5, channel.getLines().size(), "5 个有效属性 → 5 行单值行");
        String tempLine = channel.getLines().get(0);
        // tags 与 fields 均由库按字典序输出（tags: device_id < property）；行尾不携带时间戳（落库时间由 TDengine 服务器确定）
        assertEquals("device_properties_100,device_id=10001,property=temperature "
                + "create_time=1720000000000i,device_name=\"温控器01\",double_value=25.6,message_id=\"m1\"", tempLine);
        assertTrue(channel.getLines().get(1).contains(",property=count ") && channel.getLines().get(1).contains("long_value=3i"));
        assertTrue(channel.getLines().get(2).contains(",property=online ") && channel.getLines().get(2).contains("boolean_value=true"));
        assertTrue(channel.getLines().get(3).contains(",property=status ") && channel.getLines().get(3).contains("string_value=\"on\""));
        assertTrue(channel.getLines().get(4).contains(",property=dateVal ") && channel.getLines().get(4).contains("date_value=" + TS + "i"));
    }

    @Test
    void savePropertySkipsReservedAndNullAndEmpty() {
        // 保留列名/null 属性不落行
        PropertyData data = columnPropertyData();
        data.getProperties().put("message_id", "evil");
        data.getProperties().put("create_time", "2020-01-01");
        data.getProperties().put("nullProp", null);
        strategy.save(data);
        for (String line : channel.getLines()) {
            assertFalse(line.contains("evil"));
            assertFalse(line.contains("2020-01-01"));
            assertFalse(line.contains("nullProp"));
        }
        // 空属性 Map → 零行
        PropertyData empty = columnPropertyData();
        empty.setProperties(new HashMap<>());
        strategy.save(empty);
        assertEquals(5, channel.getLines().size());
    }

    @Test
    void savePropertyBlankPropertiesSkip() {
        PropertyData data = columnPropertyData();
        data.setProperties(null);
        strategy.save(data);
        assertTrue(channel.getLines().isEmpty());
    }

    // ===== 历史分页：property 为 tag 等值收窄，count + 数据两查 =====

    @Test
    void queryPropertyHistoryPage() {
        String countSql = "SELECT COUNT(*) AS `total` FROM `device_properties_100` WHERE `device_id` = '10001' AND `property` = 'temp'";
        channel.answer(countSql, java.util.Collections.singletonList(totalRow(2)));
        channel.answer("SELECT * FROM `device_properties_100` WHERE `device_id` = '10001' AND `property` = 'temp' ORDER BY `_ts` DESC LIMIT 5 OFFSET 0",
                java.util.Collections.singletonList(columnRow("temp", 3L, "long_value")));

        PageResult<PropertyData> page = strategy.queryPropertyHistory(historyQuery("temp"));

        assertEquals(2L, page.getTotal());
        assertEquals(1L, page.getPages());
        assertEquals(1, page.getList().size());
        // property tag → 属性名，单值类型列首个非空 → 属性值
        PropertyData first = page.getList().get(0);
        assertEquals(3L, first.getProperties().get("temp"));
        assertEquals("m1", first.getMessageId());
        assertEquals(TS, first.getTimestamp());
        // count 先行
        assertEquals(countSql, channel.getSqls().get(0));
        assertTrue(channel.getSqls().get(1).startsWith("SELECT *"));
    }

    @Test
    void queryPropertyHistoryPageSizeZeroCountOnly() {
        channel.answer("SELECT COUNT(*) AS `total` FROM `device_properties_100` WHERE `device_id` = '10001' AND `property` = 'temp'",
                java.util.Collections.singletonList(totalRow(2)));
        PropertyHistoryQuery query = historyQuery("temp");
        query.setPageSize(0);

        PageResult<PropertyData> page = strategy.queryPropertyHistory(query);

        assertTrue(page.getList().isEmpty());
        assertEquals(1, channel.getSqls().size(), "pageSize<=0 只发 count 不发数据 SQL");
    }

    // ===== 最新值：单值行 5 类型值列首个非空（扫描序 longvalue 优先）=====

    @Test
    void queryLatestPropertyValuesColumn() {
        String valueColumnsPrefix = "SELECT `long_value`, `string_value`, `double_value`, `date_value`, `boolean_value`, `_ts` FROM `device_properties_100` WHERE `device_id` = '10001' AND `property` = '";
        Map<String, Object> rowTemp = new HashMap<>();
        rowTemp.put("double_value", 25.6D);
        rowTemp.put("_ts", TS + 1L);
        channel.answer(valueColumnsPrefix + "temperature'", java.util.Collections.singletonList(rowTemp));
        Map<String, Object> rowStatus = new HashMap<>();
        rowStatus.put("string_value", "on");
        rowStatus.put("_ts", TS + 2L);
        channel.answer(valueColumnsPrefix + "status'", java.util.Collections.singletonList(rowStatus));

        LatestPropertyQuery query = new LatestPropertyQuery();
        query.setProductId("100");
        query.setDeviceId("10001");
        query.setPropertyIds(Arrays.asList("temperature", "status", "count"));
        Map<String, LatestPropertyValue> result = strategy.queryLatestPropertyValues(query);

        // count 无应答（从未上报）→ 结果不含；temperature/status 按入参序返回
        assertEquals(Arrays.asList("temperature", "status"), new java.util.ArrayList<>(result.keySet()));
        assertEquals(25.6D, result.get("temperature").getValue());
        assertEquals(TS + 1L, result.get("temperature").getTimestamp());
        assertEquals("on", result.get("status").getValue());
        assertEquals(TS + 2L, result.get("status").getTimestamp());
        assertEquals("SELECT `long_value`, `string_value`, `double_value`, `date_value`, `boolean_value`, `_ts` FROM `device_properties_100`"
                        + " WHERE `device_id` = '10001' AND `property` = 'temperature' ORDER BY `_ts` DESC LIMIT 1",
                channel.getSqls().get(0));
    }

    @Test
    void queryLatestPropertyValuesValueScanPriority() {
        // 行同时落多个值列（异常态）→ 扫描序 longvalue 优先取值
        Map<String, Object> row = new HashMap<>();
        row.put("long_value", 3L);
        row.put("double_value", 25.6D);
        row.put("_ts", TS);
        channel.answer("SELECT `long_value`, `string_value`, `double_value`, `date_value`, `boolean_value`, `_ts` FROM `device_properties_100` WHERE `device_id` = '10001' AND `property` = 'count'",
                java.util.Collections.singletonList(row));
        LatestPropertyQuery query = new LatestPropertyQuery();
        query.setProductId("100");
        query.setDeviceId("10001");
        query.setPropertyIds(java.util.Collections.singletonList("count"));
        assertEquals(3L, strategy.queryLatestPropertyValues(query).get("count").getValue());
    }

    @Test
    void queryLatestPropertyValuesBlankPropertyIds() {
        LatestPropertyQuery query = new LatestPropertyQuery();
        assertTrue(strategy.queryLatestPropertyValues(query).isEmpty());
        assertTrue(channel.getSqls().isEmpty());
    }

    @Test
    void getId() {
        assertEquals(StoragePolicy.TDENGINE_COLUMN, strategy.getId());
    }

    // ===== helper =====

    private static PropertyData columnPropertyData() {
        PropertyData data = new PropertyData();
        data.setDeviceId("10001");
        data.setProductId("100");
        data.setDeviceName("温控器01");
        data.setMessageId("m1");
        data.setTimestamp(TS);
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("temperature", 25.6D);
        properties.put("count", 3);
        properties.put("online", true);
        properties.put("status", "on");
        properties.put("dateVal", new Date(TS));
        data.setProperties(properties);
        return data;
    }

    /** 查询行构造 — 单值类型列仅落 valueKey 一个非空值（时间取主时间列 _ts） */
    private static Map<String, Object> columnRow(String property, Object value, String valueKey) {
        Map<String, Object> row = new HashMap<>();
        row.put("device_id", "10001");
        row.put("message_id", "m1");
        row.put("create_time", TS);
        row.put("device_name", "温控器01");
        row.put("_ts", TS);
        row.put("property", property);
        row.put(valueKey, value);
        return row;
    }

    private static PropertyHistoryQuery historyQuery(String property) {
        PropertyHistoryQuery query = new PropertyHistoryQuery();
        query.setProductId("100");
        query.setDeviceId("10001");
        query.setProperty(property);
        query.setPageNum(1);
        query.setPageSize(5);
        return query;
    }

    private static Map<String, Object> totalRow(long total) {
        Map<String, Object> row = new HashMap<>();
        row.put("total", total);
        return row;
    }
}
