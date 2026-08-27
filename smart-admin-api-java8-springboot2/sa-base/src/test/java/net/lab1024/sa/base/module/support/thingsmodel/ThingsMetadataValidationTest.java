package net.lab1024.sa.base.module.support.thingsmodel;

import net.lab1024.sa.base.metadata.ThingsMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 物模型校验测试（见物模型校验设计.md 八章）— 覆盖 5 个校验入口：
 * 读属性 / 写属性 / 功能调用 / 属性上报 / 事件上报，含 DataType.validateValue 递归（int 范围、enum 枚举值、object/array 子项）。
 * 物模型经 IotThingsMetadataCodec 从 JSON 构造，与生产解析链路一致。
 *
 * @Author 廖涛
 * @Date 2026/08/27
 * @Copyright 1024创新实验室
 */
class ThingsMetadataValidationTest {

    /**
     * 测试物模型：
     * - temp:  int 0~100，可写
     * - version: string maxLength 10，只读
     * - config: object（mode enum auto/manual + level int 1~5）
     * - sensors: array of double min 0
     * - setSpeed: 功能（speed int 0~1000 必填 + gear enum low/high 可选）
     * - alarm: 事件（code int + time date）
     */
    private static final String METADATA_JSON = "{"
            + "\"properties\":["
            + "{\"id\":\"temp\",\"name\":\"温度\",\"accessMode\":\"rw\",\"valueType\":{\"type\":\"int\",\"min\":0,\"max\":100}},"
            + "{\"id\":\"version\",\"name\":\"版本\",\"accessMode\":\"r\",\"valueType\":{\"type\":\"string\",\"maxLength\":10}},"
            + "{\"id\":\"name\",\"name\":\"设备名\",\"accessMode\":\"rw\",\"valueType\":{\"type\":\"string\",\"maxLength\":10}},"
            + "{\"id\":\"alarmState\",\"name\":\"告警状态\",\"accessMode\":\"rw\",\"valueType\":{\"type\":\"boolean\",\"trueValue\":\"ON\",\"falseValue\":\"OFF\"}},"
            + "{\"id\":\"config\",\"name\":\"配置\",\"accessMode\":\"rw\",\"valueType\":{\"type\":\"object\",\"properties\":["
            + "{\"id\":\"mode\",\"name\":\"模式\",\"valueType\":{\"type\":\"enum\",\"elements\":[{\"value\":\"auto\",\"text\":\"自动\"},{\"value\":\"manual\",\"text\":\"手动\"}]}},"
            + "{\"id\":\"level\",\"name\":\"档位\",\"valueType\":{\"type\":\"int\",\"min\":1,\"max\":5}}]}},"
            + "{\"id\":\"sensors\",\"name\":\"传感器\",\"accessMode\":\"rw\",\"valueType\":{\"type\":\"array\",\"elementType\":{\"type\":\"double\",\"min\":0}}}"
            + "],"
            + "\"functions\":["
            + "{\"id\":\"setSpeed\",\"name\":\"设置速度\",\"inputs\":["
            + "{\"id\":\"speed\",\"name\":\"速度\",\"required\":true,\"valueType\":{\"type\":\"int\",\"min\":0,\"max\":1000}},"
            + "{\"id\":\"gear\",\"name\":\"档位\",\"required\":false,\"valueType\":{\"type\":\"enum\",\"elements\":[{\"value\":\"low\",\"text\":\"低\"},{\"value\":\"high\",\"text\":\"高\"}]}}]}"
            + "],"
            + "\"events\":["
            + "{\"id\":\"alarm\",\"name\":\"告警\",\"valueType\":{\"type\":\"object\",\"properties\":["
            + "{\"id\":\"code\",\"name\":\"编码\",\"valueType\":{\"type\":\"int\"}},"
            + "{\"id\":\"time\",\"name\":\"时间\",\"valueType\":{\"type\":\"date\"}}]}}"
            + "]}";

    private ThingsMetadata metadata;

    @BeforeEach
    void setUp() {
        metadata = IotThingsMetadataCodec.getInstance().decode(METADATA_JSON);
    }

    // ===== 读属性 =====

    @Test
    void readProperties() {
        // 合法：全部存在 → 空错误
        assertTrue(metadata.validateReadProperties(Arrays.asList("temp", "version")).isEmpty());
        // 不存在的属性 → 报错
        List<String> errors = metadata.validateReadProperties(Arrays.asList("temp", "unknown"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [unknown] 不存在"));
    }

    // ===== 写属性 =====

    @Test
    void writePropertiesValid() {
        // 合法值（int 范围内 / object 子属性合法 / array 元素合法）→ 空错误
        assertTrue(metadata.validateProperties(map("temp", 50)).isEmpty());
        assertTrue(metadata.validateProperties(map("temp", 50, "config",
                map("mode", "auto", "level", 3), "sensors", Arrays.asList(1.5, 2.0))).isEmpty());
    }

    @Test
    void writePropertiesNotExist() {
        List<String> errors = metadata.validateProperties(map("unknown", 1));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [unknown] 不存在"));
    }

    @Test
    void writePropertiesReadOnly() {
        // 只读属性 → 不可写
        List<String> errors = metadata.validateProperties(map("version", "v1"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [version] 为只读，不可写"));
    }

    @Test
    void writePropertiesValueTypeAndRange() {
        // 值类型错误：int 属性传字符串
        List<String> errors = metadata.validateProperties(map("temp", "50"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [temp] 类型应为 int"));
        // 超范围：> max
        errors = metadata.validateProperties(map("temp", 150));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [temp] 不能大于 100"));
        // 字符串超长
        errors = metadata.validateProperties(map("name", "12345678901"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [name] 长度不能超过 10"));
    }

    @Test
    void writePropertiesObjectRecursive() {
        // object 子属性递归：enum 非法值 + int 超范围 → 一次返回全部错误
        List<String> errors = metadata.validateProperties(map("config", map("mode", "autoX", "level", 9)));
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(e -> e.contains("属性 [config] properties[mode] 不在枚举值范围内")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("属性 [config] properties[level] 不能大于 5")));
        // object 整体类型错误
        errors = metadata.validateProperties(map("config", "abc"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [config] 类型应为 object"));
    }

    @Test
    void writePropertiesBooleanProtocolValue() {
        // 命中 trueValue/falseValue（协议值形态）→ 合法
        assertTrue(metadata.validateProperties(map("alarmState", "ON")).isEmpty());
        assertTrue(metadata.validateProperties(map("alarmState", "OFF")).isEmpty());
        // 原生布尔 → 不合法（仅接受 trueValue/falseValue 之一，报错带字段配置值）
        List<String> errors = metadata.validateProperties(map("alarmState", true));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [alarmState] 应为 trueValue[ON] 或 falseValue[OFF] 之一"));
        // 未命中两者 → 报错
        errors = metadata.validateProperties(map("alarmState", "YES"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [alarmState] 应为 trueValue[ON] 或 falseValue[OFF] 之一"));
    }

    @Test
    void writePropertiesArrayRecursive() {
        // array 逐元素递归：元素 2 超范围 → 报错带下标
        List<String> errors = metadata.validateProperties(map("sensors", Arrays.asList(1.5, -1.0)));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [sensors] elements[1] 不能小于 0"));
    }

    // ===== 功能调用 =====

    @Test
    void functionValid() {
        // 合法：必填齐全 + 值合法 → 空错误
        assertTrue(metadata.validateFunction("setSpeed", map("speed", 100)).isEmpty());
        assertTrue(metadata.validateFunction("setSpeed", map("speed", 100, "gear", "high")).isEmpty());
    }

    @Test
    void functionNotExist() {
        List<String> errors = metadata.validateFunction("noSuchFn", Collections.emptyMap());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("功能 [noSuchFn] 不存在"));
    }

    @Test
    void functionUnknownParam() {
        List<String> errors = metadata.validateFunction("setSpeed", map("speed", 100, "xxx", 1));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("功能 [setSpeed] 不存在参数 [xxx]"));
    }

    @Test
    void functionMissingRequired() {
        List<String> errors = metadata.validateFunction("setSpeed", Collections.emptyMap());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("功能 [setSpeed] 缺少必填参数 [speed]"));
    }

    @Test
    void functionValueTypeAndEnum() {
        // 参数值类型错误
        List<String> errors = metadata.validateFunction("setSpeed", map("speed", "100"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("参数 [speed] 类型应为 int"));
        // 参数 enum 非法值
        errors = metadata.validateFunction("setSpeed", map("speed", 100, "gear", "middle"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("参数 [gear] 不在枚举值范围内"));
    }

    // ===== 属性上报 =====

    @Test
    void reportProperties() {
        // 只读属性（accessMode=r）上报不报错（只读仅限制平台下发）
        assertTrue(metadata.validateReportProperties(map("version", "v1")).isEmpty());
        // 合法值 → 空错误
        assertTrue(metadata.validateReportProperties(map("temp", 50)).isEmpty());
        // 不存在的属性 → 报错
        List<String> errors = metadata.validateReportProperties(map("unknown", 1));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [unknown] 不存在"));
        // 非法值 → 报错
        errors = metadata.validateReportProperties(map("temp", "abc"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("属性 [temp] 类型应为 int"));
    }

    // ===== 事件上报 =====

    @Test
    void event() {
        // 合法数据（int + date 格式正确）→ 空错误
        assertTrue(metadata.validateEvent("alarm",
                map("code", 1, "time", "2026-08-27 10:00:00")).isEmpty());
        // 时间戳（毫秒/秒均可）→ 空错误
        assertTrue(metadata.validateEvent("alarm", map("code", 1, "time", 1785200000000L)).isEmpty());
        assertTrue(metadata.validateEvent("alarm", map("code", 1, "time", 1785200000L)).isEmpty());
        // 事件不存在 → 报错
        List<String> errors = metadata.validateEvent("noSuchEvent", Collections.emptyMap());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("事件 [noSuchEvent] 不存在"));
        // object 子属性非法 → 递归报错
        errors = metadata.validateEvent("alarm", map("code", "x"));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("事件 [alarm] properties[code] 类型应为 int"));
        // 事件整体类型错误（非 object）
        errors = metadata.validateEvent("alarm", "x");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("事件 [alarm] 类型应为 object"));
    }

    @Test
    void eventDateTimestamp() {
        // 负数时间戳 → 不合法
        List<String> errors = metadata.validateEvent("alarm", map("code", 1, "time", -1L));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("事件 [alarm] properties[time] 时间戳不合法"));
        // 非数字非字符串（时间戳字段传布尔）→ 类型错误
        errors = metadata.validateEvent("alarm", map("code", 1, "time", true));
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("事件 [alarm] properties[time] 类型应为 date 字符串或时间戳"));
    }

    // ===== 辅助 =====

    @SafeVarargs
    private static Map<String, Object> map(Object... kv) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((String) kv[i], kv[i + 1]);
        }
        return map;
    }
}
