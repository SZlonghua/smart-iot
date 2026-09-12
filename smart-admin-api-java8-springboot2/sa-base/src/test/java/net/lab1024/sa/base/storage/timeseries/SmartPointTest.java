package net.lab1024.sa.base.storage.timeseries;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 业务侧行协议点测试 — builder 构造转行协议：类型分派落列（i 整数/裸浮点/引号字符串/布尔/Date 毫秒）、
 * tag 原样不带引号、measurement sanitize、null 键值缺省、行尾不携带时间戳（落库时间由 TDengine 服务器确定）。
 * <p>
 * 行协议由 InfluxDB Point 序列化：tags 与 fields 均按字典序输出（与插入序无关）；
 * 断言按行协议片段定位，不依赖库行为之外的排序细节。
 *
 * @Author 廖涛
 * @Date 2026/09/09
 * @Copyright 1024创新实验室
 */
class SmartPointTest {

    private static final long TS = 1720000000000L;

    @Test
    void sanitizeMeasurement() {
        assertEquals("device_properties_100", SmartPoint.sanitizeMeasurement("device_properties_100"));
        // 非法字符替换为 _
        assertEquals("a_b_c", SmartPoint.sanitizeMeasurement("a b.c"));
        // 数字开头加 d_ 前缀
        assertEquals("d_100abc", SmartPoint.sanitizeMeasurement("100abc"));
        assertEquals("", SmartPoint.sanitizeMeasurement(null));
    }

    @Test
    void columnTypeDispatch() {
        assertEquals(3L, SmartPoint.column(3));
        assertEquals(3L, SmartPoint.column((byte) 3));
        // float 经 doubleValue() 提升为 double（25.6f 二进制表示 → 25.600000381469727）
        assertEquals(25.600000381469727D, SmartPoint.column(25.6F));
        assertEquals(25.6D, SmartPoint.column(new java.math.BigDecimal("25.6")));
        assertEquals(Boolean.TRUE, SmartPoint.column(true));
        assertEquals(TS, SmartPoint.column(new Date(TS)));
        assertEquals("on", SmartPoint.column("on"));
        assertEquals("a", SmartPoint.column('a'));
        assertEquals(null, SmartPoint.column(null));
    }

    @Test
    void toLineProtocol() {
        String line = SmartPoint.builder()
                .table("device_properties_100")
                .tag("device_id", "10001")
                .field("message_id", "m1")
                .field("temperature", 25.6D)
                .field("count", 3L)
                .field("status", "on")
                .field("online", true)
                .field("tsField", new Date(TS))
                .field("nullField", null)   // null 值缺省
                .field("skipNullKey", null)
                .field(null, "x")           // null 键缺省（builder 忽略）
                .build()
                .toLineProtocol();

        // tags 与 fields 均由库按字典序输出（与插入序无关）；null 值与 null 键缺省不落行；行尾无时间戳
        assertEquals("device_properties_100,device_id=10001 count=3i,message_id=\"m1\",online=true,status=\"on\",temperature=25.6,tsField=" + TS + "i", line);
    }

    @Test
    void builderFieldsBatchAdd() {
        // fields(Map) 批量灌入 — 键/值 null 与保留列名（message_id/create_time/device_name）自动忽略；
        // 消息元数据保留列由显式 field() 先落，属性集同名键不会覆盖
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("temperature", 25.6D);
        props.put("nullProp", null);
        props.put("message_id", "evil");
        props.put("create_time", "2020-01-01");
        props.put("temperature", 26.6D);
        String line = SmartPoint.builder()
                .table("device_properties_100")
                .tag("device_id", "10001")
                .field("message_id", "m1")
                .fields(props)
                .build()
                .toLineProtocol();
        assertTrue(line.contains("temperature=26.6"), "重复键后者覆盖: " + line);
        assertTrue(line.contains("message_id=\"m1\""), "显式保留列不受批量属性影响: " + line);
        assertFalse(line.contains("evil"), "批量属性含保留列名应被忽略: " + line);
        assertFalse(line.contains("2020-01-01"), "批量属性含保留列名应被忽略: " + line);
        assertFalse(line.contains("nullProp"), "null 值不落行: " + line);
    }

    @Test
    void toLineProtocolEscapes() {
        // 字符串字段值中的引号/反斜杠由库转义，tags 值不带引号
        String line = SmartPoint.builder()
                .table("device_properties_100")
                .tag("device_id", "10001")
                .field("name", "hello \"x\" y")
                .build()
                .toLineProtocol();
        assertTrue(line.startsWith("device_properties_100,device_id=10001 "));
        assertTrue(line.contains("name=\"hello \\\"x\\\" y\""), "引号应转义: " + line);
        assertFalse(line.endsWith(" " + TS), "行尾不携带时间戳: " + line);
    }

    @Test
    void toLineProtocolObjectFieldCompactJson() {
        // 集合/对象值 → 紧凑 JSON 字符串列
        Map<String, Object> complex = new LinkedHashMap<>();
        complex.put("a", 1);
        String line = SmartPoint.builder()
                .table("t")
                .tag("device_id", "1")
                .field("cfg", complex)
                .build()
                .toLineProtocol();
        assertTrue(line.contains("cfg=\"{\\\"a\\\":1}\""), line);
        assertFalse(line.contains("\n"), "单行协议不应含换行");
    }

    @Test
    void toLineProtocolWithoutTags() {
        String line = SmartPoint.builder()
                .table("device_properties_100")
                .field("count", 3L)
                .build()
                .toLineProtocol();
        assertEquals("device_properties_100 count=3i", line);
    }
}
