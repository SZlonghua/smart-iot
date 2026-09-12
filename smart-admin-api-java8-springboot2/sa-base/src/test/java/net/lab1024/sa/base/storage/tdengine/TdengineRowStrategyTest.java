package net.lab1024.sa.base.storage.tdengine;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.storage.StoragePolicy;
import net.lab1024.sa.base.storage.model.CommandLogQuery;
import net.lab1024.sa.base.storage.model.EventData;
import net.lab1024.sa.base.storage.model.EventHistoryQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyValue;
import net.lab1024.sa.base.storage.model.MessageLogData;
import net.lab1024.sa.base.storage.model.PropertyData;
import net.lab1024.sa.base.storage.model.PropertyHistoryQuery;
import net.lab1024.sa.base.storage.model.StorageData;
import net.lab1024.sa.base.storage.timeseries.FakeTimeseriesChannel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TDengine 行布局策略测试 — 属性一条消息一行（摊平列）、事件/日志固定列（tags 追加 event/dataType/commandType）、
 * 共享保留列（messageId/create_time/deviceName 冗余），消息时间落 create_time 列、落库时间由 TDengine 服务器写入主时间列 _ts
 * （行协议不携带时间戳）；历史属性按行整行取回解码后过滤、
 * 最新值每属性独立查列、事件/日志分页两查（count 短路）。经 FakeTimeseriesChannel 断言精确行协议与 SQL。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
class TdengineRowStrategyTest {

    private static final long TS = 1720000000000L;
    private static final String LOG_TABLE = "device_message_logs_100";

    private final FakeTimeseriesChannel channel = new FakeTimeseriesChannel();
    private final TdengineRowStrategy strategy = new TdengineRowStrategy(channel);

    // ===== 写：三种消息类型 → 行协议 =====

    @Test
    void savePropertyRowLine() {
        strategy.save(propertyData());
        assertEquals(1, channel.getLines().size());
        String line = channel.getLines().get(0);
        // tags 与 fields 均由库按字典序输出；行尾不携带时间戳（落库时间由 TDengine 服务器确定）
        assertEquals("device_properties_100,device_id=10001 count=3i"
                        + ",create_time=1720000000000i,device_name=\"温控器01\",message_id=\"m1\",online=true,status=\"on\",temperature=25.6",
                line);
    }

    @Test
    void savePropertySkipsReservedAndNull() {
        PropertyData data = propertyData();
        data.getProperties().put("message_id", "evil");   // 保留列名跳过
        data.getProperties().put("create_time", "2020-01-01");   // 保留列名跳过
        data.getProperties().put("nullProp", null);       // 空值跳过
        strategy.save(data);
        String line = channel.getLines().get(0);
        assertFalse(line.contains("evil"));
        assertFalse(line.contains("2020-01-01"));
        assertFalse(line.contains("nullProp"));
    }

    @Test
    void saveEventRowLine() {
        EventData data = eventData();
        strategy.save(data);
        assertEquals(1, channel.getLines().size());
        String line = channel.getLines().get(0);
        // tags 与 fields 均由库按字典序输出（tags: data_type < device_id < event）
        assertEquals("device_events_100,data_type=object,device_id=10001,event=temp_alarm "
                        + "create_time=1720000000000i,data=\"{\\\"temp\\\":68}\",device_name=\"温控器01\",message_id=\"m1\",type=\"warning\"",
                line);
    }

    @Test
    void saveMessageLogRowLine() {
        strategy.save(logData(MessageLogData.DIRECTION_DOWN));
        assertEquals(1, channel.getLines().size());
        String line = channel.getLines().get(0);
        // tags 与 fields 均由库按字典序输出（tags: command_type < device_id）
        assertEquals(LOG_TABLE + ",command_type=writeProperty,device_id=10001 " + "content=\"{\\\"success\\\":false}\""
                + ",create_time=1720000000000i,device_name=\"温控器01\",direction=\"down\",message_id=\"m1\"", line);
    }

    @Test
    void saveDispatchSkips() {
        // 空/缺产品维度：不写行不报错
        strategy.save(null);
        PropertyData noProduct = propertyData();
        noProduct.setProductId(null);
        strategy.save(noProduct);
        strategy.save(new StorageData() {
        });
        assertTrue(channel.getLines().isEmpty());
    }

    // ===== 属性历史分页（{property} IS NOT NULL 收窄，count + 数据两查）=====

    @Test
    void queryPropertyHistoryPage() {
        String countSql = "SELECT COUNT(*) AS `total` FROM `device_properties_100` WHERE `device_id` = '10001' AND `temperature` IS NOT NULL";
        channel.answer(countSql, java.util.Collections.singletonList(totalRow(2)));
        channel.answer("SELECT * FROM `device_properties_100` WHERE `device_id` = '10001' AND `temperature` IS NOT NULL ORDER BY `_ts` DESC LIMIT 5 OFFSET 0",
                Arrays.asList(cannedPropertyRow("m1", "temperature", 25.6D),
                        cannedPropertyRow("m2", "temperature", 26.1D)));

        PageResult<PropertyData> page = strategy.queryPropertyHistory(historyQuery("temperature"));

        assertEquals(2L, page.getTotal());
        assertEquals(1L, page.getPages());
        assertEquals(2, page.getList().size());
        // 整行解码：_ts/保留列剥离，其余列为属性（行集已由 IS NOT NULL 保证含该属性）
        PropertyData first = page.getList().get(0);
        assertFalse(first.getProperties().containsKey("_ts"));
        assertFalse(first.getProperties().containsKey("device_id"));
        assertFalse(first.getProperties().containsKey("create_time"));
        assertEquals(25.6D, first.getProperties().get("temperature"));
        assertEquals("m1", first.getMessageId());
        assertEquals(TS, first.getTimestamp());
        // count 先行、数据页 SQL 后发
        assertEquals(countSql, channel.getSqls().get(0));
        assertTrue(channel.getSqls().get(1).startsWith("SELECT *"));
    }

    @Test
    void queryPropertyHistoryPageSizeZeroCountOnly() {
        channel.answer("SELECT COUNT(*) AS `total` FROM `device_properties_100` WHERE `device_id` = '10001' AND `temperature` IS NOT NULL",
                java.util.Collections.singletonList(totalRow(2)));
        PropertyHistoryQuery query = historyQuery("temperature");
        query.setPageSize(0);

        PageResult<PropertyData> page = strategy.queryPropertyHistory(query);

        assertTrue(page.getList().isEmpty());
        assertEquals(1, channel.getSqls().size(), "pageSize<=0 只发 count 不发数据 SQL");
    }

    // ===== 最新属性值（每属性独立查列 _ts 倒序首行）=====

    @Test
    void queryLatestPropertyValuesRow() {
        Map<String, Object> rowTemp = new HashMap<>();
        rowTemp.put("temperature", 30.5D);
        rowTemp.put("_ts", TS + 5L);
        channel.answer("SELECT `temperature`, `_ts` FROM `device_properties_100`", java.util.Collections.singletonList(rowTemp));
        // humidity 无应答（属性列不存在/未上报）→ 结果不含

        LatestPropertyQuery query = new LatestPropertyQuery();
        query.setProductId("100");
        query.setDeviceId("10001");
        query.setPropertyIds(Arrays.asList("temperature", "humidity"));
        Map<String, LatestPropertyValue> result = strategy.queryLatestPropertyValues(query);

        // humidity 无应答（列不存在/未上报）→ 结果不含
        assertEquals(java.util.Collections.singletonList("temperature"), new ArrayList<>(result.keySet()));
        LatestPropertyValue temp = result.get("temperature");
        assertEquals(30.5D, temp.getValue());
        assertEquals(TS + 5L, temp.getTimestamp());
        assertEquals("SELECT `humidity`, `_ts` FROM `device_properties_100` WHERE `device_id` = '10001' AND `humidity` IS NOT NULL ORDER BY `_ts` DESC LIMIT 1",
                channel.getSqls().get(1));
    }

    /** 动态属性列名一律反引号引用 — 数值名（裸写被解析为数值字面量）与驼峰名（裸写被小写化）均按列取值 */
    @Test
    void queryLatestPropertyValuesQuotesDynamicColumn() {
        LatestPropertyQuery query = new LatestPropertyQuery();
        query.setProductId("100");
        query.setDeviceId("10001");
        query.setPropertyIds(Arrays.asList("11", "tempValue"));

        assertTrue(strategy.queryLatestPropertyValues(query).isEmpty());
        assertEquals("SELECT `11`, `_ts` FROM `device_properties_100` WHERE `device_id` = '10001' AND `11` IS NOT NULL ORDER BY `_ts` DESC LIMIT 1",
                channel.getSqls().get(0));
        assertEquals("SELECT `tempValue`, `_ts` FROM `device_properties_100` WHERE `device_id` = '10001' AND `tempValue` IS NOT NULL ORDER BY `_ts` DESC LIMIT 1",
                channel.getSqls().get(1));
    }

    @Test
    void queryLatestPropertyValuesBlankPropertyIds() {
        LatestPropertyQuery query = new LatestPropertyQuery();
        query.setPropertyIds(null);
        assertTrue(strategy.queryLatestPropertyValues(query).isEmpty());
        assertTrue(channel.getSqls().isEmpty());
    }

    // ===== 事件分页（count + 数据两查，count 短路）=====

    @Test
    void queryEventHistoryPage() {
        channel.answer("SELECT COUNT(*) AS `total` FROM `device_events_100`",
                java.util.Collections.singletonList(totalRow(1)));
        channel.answer("SELECT * FROM `device_events_100`", java.util.Collections.singletonList(cannedEventRow()));

        EventHistoryQuery query = new EventHistoryQuery();
        query.setProductId("100");
        query.setDeviceId("10001");
        query.setEvent("temp_alarm");
        query.setStartTime(100L);
        query.setEndTime(200L);
        query.setPageNum(1);
        query.setPageSize(10);
        PageResult<EventData> page = strategy.queryEventHistory(query);

        assertEquals(1L, page.getTotal());
        assertEquals(1L, page.getPages());
        assertEquals(1, page.getList().size());
        EventData data = page.getList().get(0);
        assertEquals("10001", data.getDeviceId());
        assertEquals("m1", data.getMessageId());
        assertEquals("temp_alarm", data.getEvent());
        assertEquals("warning", data.getType());
        assertEquals("object", data.getDataType());
        assertEquals("{\"temp\":68}", data.getData());
        assertEquals(TS, data.getTimestamp());
        assertEquals("SELECT COUNT(*) AS `total` FROM `device_events_100` WHERE `device_id` = '10001' AND `_ts` >= 100 AND `_ts` <= 200 AND `event` = 'temp_alarm'",
                channel.getSqls().get(0));
        assertEquals("SELECT * FROM `device_events_100` WHERE `device_id` = '10001' AND `_ts` >= 100 AND `_ts` <= 200 AND `event` = 'temp_alarm' ORDER BY `_ts` DESC LIMIT 10 OFFSET 0",
                channel.getSqls().get(1));
    }

    @Test
    void queryEventHistoryTotalZeroShortCircuit() {
        channel.answer("SELECT COUNT(*)", java.util.Collections.singletonList(totalRow(0)));
        PageResult<EventData> page = strategy.queryEventHistory(baseEventQuery());
        assertEquals(0L, page.getTotal());
        assertTrue(page.getList().isEmpty());
        assertTrue(page.getEmptyFlag());
        assertEquals(1, channel.getSqls().size(), "total=0 时不发数据查询");
    }

    // ===== 命令日志分页（direction/deviceName/commandTypes IN 条件）=====

    @Test
    void queryCommandLogHistoryConditions() {
        channel.answer("SELECT COUNT(*) AS `total` FROM `" + LOG_TABLE + "`", java.util.Collections.singletonList(totalRow(2)));
        Map<String, Object> downRow = cannedLogRow("m1", "down", "writeProperty");
        Map<String, Object> upRow = cannedLogRow("m1", "up", "writePropertyReply");
        upRow.put("content", "{\"success\":true,\"properties\":{}}");
        channel.answer("SELECT * FROM `" + LOG_TABLE + "`", Arrays.asList(downRow, upRow));

        CommandLogQuery query = new CommandLogQuery();
        query.setProductId("100");
        query.setDeviceId("10001");
        query.setDirection("down");
        query.setDeviceName("O'Brien");
        query.setCommandTypes(Arrays.asList("writeProperty", "writePropertyReply"));
        query.setPageNum(1);
        query.setPageSize(5);
        PageResult<MessageLogData> page = strategy.queryCommandLogHistory(query);

        assertEquals(2L, page.getTotal());
        assertEquals("SELECT COUNT(*) AS `total` FROM `" + LOG_TABLE
                        + "` WHERE `device_id` = '10001' AND `direction` = 'down' AND `device_name` = 'O''Brien' AND `command_type` IN ('writeProperty','writePropertyReply')",
                channel.getSqls().get(0));
        assertEquals("SELECT * FROM `" + LOG_TABLE
                        + "` WHERE `device_id` = '10001' AND `direction` = 'down' AND `device_name` = 'O''Brien' AND `command_type` IN ('writeProperty','writePropertyReply') ORDER BY `_ts` DESC LIMIT 5 OFFSET 0",
                channel.getSqls().get(1));
        MessageLogData first = page.getList().get(0);
        assertEquals("down", first.getDirection());
        assertEquals("writeProperty", first.getCommandType());
        assertEquals("m1", first.getMessageId());
    }

    @Test
    void queryCommandLogHistoryBlankConditions() {
        channel.answer("SELECT COUNT(*)", java.util.Collections.singletonList(totalRow(0)));
        CommandLogQuery query = new CommandLogQuery();
        query.setProductId("100");
        query.setDeviceId("10001");
        query.setPageNum(1);
        query.setPageSize(5);
        PageResult<MessageLogData> page = strategy.queryCommandLogHistory(query);
        assertEquals(0L, page.getTotal());
        // 无业务条件仅 deviceId
        assertTrue(channel.getSqls().get(0).contains("WHERE `device_id` = '10001'"));
    }

    @Test
    void getId() {
        assertEquals(StoragePolicy.TDENGINE_ROW, strategy.getId());
    }

    // ===== helper =====

    private static PropertyData propertyData() {
        PropertyData data = new PropertyData();
        data.setDeviceId("10001");
        data.setProductId("100");
        data.setDeviceName("温控器01");
        data.setMessageId("m1");
        data.setTimestamp(TS);
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("temperature", 25.6D);
        properties.put("count", 3L);
        properties.put("status", "on");
        properties.put("online", true);
        data.setProperties(properties);
        return data;
    }

    private static EventData eventData() {
        EventData data = new EventData();
        data.setDeviceId("10001");
        data.setProductId("100");
        data.setDeviceName("温控器01");
        data.setMessageId("m1");
        data.setTimestamp(TS);
        data.setEvent("temp_alarm");
        data.setType("warning");
        data.setDataType("object");
        data.setData("{\"temp\":68}");
        return data;
    }

    private static MessageLogData logData(String direction) {
        MessageLogData data = new MessageLogData();
        data.setDeviceId("10001");
        data.setProductId("100");
        data.setDeviceName("温控器01");
        data.setMessageId("m1");
        data.setTimestamp(TS);
        data.setDirection(direction);
        data.setCommandType("writeProperty");
        data.setContent("{\"success\":false}");
        return data;
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

    private static Map<String, Object> cannedPropertyRow(String messageId, String property, Object value) {
        Map<String, Object> row = new HashMap<>();
        row.put("device_id", "10001");
        row.put("message_id", messageId);
        row.put("create_time", TS);
        row.put("device_name", "温控器01");
        row.put("_ts", TS);
        row.put(property, value);
        return row;
    }

    private static Map<String, Object> cannedEventRow() {
        Map<String, Object> row = new HashMap<>();
        row.put("device_id", "10001");
        row.put("message_id", "m1");
        row.put("create_time", TS);
        row.put("device_name", "温控器01");
        row.put("_ts", TS);
        row.put("event", "temp_alarm");
        row.put("type", "warning");
        row.put("data_type", "object");
        row.put("data", "{\"temp\":68}");
        return row;
    }

    private static Map<String, Object> cannedLogRow(String messageId, String direction, String commandType) {
        Map<String, Object> row = new HashMap<>();
        row.put("device_id", "10001");
        row.put("message_id", messageId);
        row.put("create_time", TS);
        row.put("device_name", "温控器01");
        row.put("_ts", TS);
        row.put("direction", direction);
        row.put("command_type", commandType);
        row.put("content", "{\"success\":false}");
        return row;
    }

    private static Map<String, Object> totalRow(long total) {
        Map<String, Object> row = new HashMap<>();
        row.put("total", total);
        return row;
    }

    private static EventHistoryQuery baseEventQuery() {
        EventHistoryQuery query = new EventHistoryQuery();
        query.setProductId("100");
        query.setDeviceId("10001");
        query.setPageNum(1);
        query.setPageSize(10);
        return query;
    }
}
