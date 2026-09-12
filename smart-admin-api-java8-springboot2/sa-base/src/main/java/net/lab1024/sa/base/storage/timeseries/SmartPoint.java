package net.lab1024.sa.base.storage.timeseries;

import com.influxdb.client.write.Point;
import net.lab1024.sa.base.module.support.json.JsonUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 业务侧行协议点 — 一条写入行（逻辑表名 + tags + fields）的自封装模型。
 * <p>
 * builder 模式构造（{@link #builder()}），{@link #toLineProtocol()} 内部借 InfluxDB Point
 * 序列化为行协议字符串（键排序、转义与类型后缀由库处理，不手写拼接）——save 落库路径只需
 * 构造本点即得可写行协议，不直接感知 InfluxDB 类型。
 * <p>
 * 行协议末尾不携带时间戳 —— 主时间列 _ts 由 TDengine 落库时取服务器当前时间写入；
 * 消息自身时间走普通字段列随 fields 落库（如 create_time）。
 * <p>
 * tags 由 Point 输出不带引号（InfluxDB 风格），TDengine schemaless 按位置解析为 NCHAR；
 * fields 值经 {@link #column(Object)} 类型分派后落列：整数→BIGINT、浮点→DOUBLE、Boolean→原生、
 * 文本→引号串、集合/对象→紧凑 JSON 串、Date→毫秒 i64；null 值缺省不落列（含键为 null）。
 * 纯值对象无运行时依赖，可单测。
 *
 * @Author 廖涛
 * @Date 2026/09/09
 * @Copyright 1024创新实验室
 */
public final class SmartPoint {

    /** 表名/measurement 非法字符（非字母数字下划线） */
    private static final Pattern ILLEGAL_MEASUREMENT_CHAR = Pattern.compile("[^a-zA-Z0-9_]");

    private final String table;
    private final Map<String, String> tags;
    private final Map<String, Object> fields;

    private SmartPoint(Builder builder) {
        this.table = builder.table;
        this.tags = builder.tags;
        this.fields = builder.fields;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 表名 sanitize — 非字母数字下划线替换为 _，数字开头加 d_ 前缀（TDengine 表名约束：不能数字开头）
     */
    public static String sanitizeMeasurement(String table) {
        String sanitized = table == null ? "" : ILLEGAL_MEASUREMENT_CHAR.matcher(table).replaceAll("_");
        if (!sanitized.isEmpty() && sanitized.charAt(0) >= '0' && sanitized.charAt(0) <= '9') {
            return "d_" + sanitized;
        }
        return sanitized;
    }

    /**
     * Java 值 → 落列值类型分派（与 {@link TimeseriesLayout#valueColumnName} 分派对齐；null 缺省）：
     * 整数（byte/short/int/long/BigInteger）→ long i64；浮点（float/double/BigDecimal）→ double f64；
     * Boolean 原样；Date → 毫秒 i64；字符/枚举/其他 → toString；集合/Map/数组/其他对象 → 紧凑 JSON 字符串。
     */
    public static Object column(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer
                || value instanceof Long || value instanceof BigInteger) {
            return ((Number) value).longValue();
        }
        if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof Boolean) {
            return value;
        }
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        if (value instanceof CharSequence || value instanceof Character) {
            return value.toString();
        }
        return JsonUtil.toJson(value);
    }

    /**
     * 转为行协议字符串（measurement = sanitize 后表名；tags 原样、fields 经 column 分派且 null 缺省；
     * 行尾无时间戳 → TDengine 落库时以服务器当前时间为 _ts）— 表名与 deviceId tag 恒非空，正常数据必有输出
     */
    public String toLineProtocol() {
        Point point = Point.measurement(sanitizeMeasurement(table));
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                point.addTag(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey();
            Object value = column(entry.getValue());
            if (key == null || value == null) {
                continue;
            }
            if (value instanceof Boolean) {
                point.addField(key, (Boolean) value);
            } else if (value instanceof Number) {
                point.addField(key, (Number) value);
            } else {
                point.addField(key, value.toString());
            }
        }
        // 不调 point.time(...)：行协议不带时间戳，TDengine 以服务器当前时间落主时间列 _ts
        return point.toLineProtocol();
    }

    /**
     * 行点构造器 — 标签/字段为空值（键或值 null）时该维度缺省不落行
     */
    public static final class Builder {

        private String table;
        private final Map<String, String> tags = new HashMap<>();
        private final Map<String, Object> fields = new HashMap<>();

        private Builder() {
        }

        public Builder table(String table) {
            this.table = table;
            return this;
        }

        /** 维度标签（deviceId/property 等；键或值 null 忽略） */
        public Builder tag(String key, String value) {
            if (key != null && value != null) {
                tags.put(key, value);
            }
            return this;
        }

        /** 数据字段（值经类型分派落列；键或值 null 忽略） */
        public Builder field(String key, Object value) {
            if (key != null && value != null) {
                fields.put(key, value);
            }
            return this;
        }

        /**
         * 数据字段（批量 Map）— 逐个走 {@link #field(String, Object)} 语义：键/值 null 自动忽略；
         * 保留列名（messageId/create_time/deviceName）自动跳过 —— 本方法面向属性动态列整批灌入，
         * 属性不得覆盖消息元数据列；消息元数据（保留列）一律经显式 {@link #field(String, Object)} 添加
         */
        public Builder fields(Map<String, Object> fields) {
            if (fields != null) {
                for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    String key = entry.getKey();
                    if (key != null && !TimeseriesLayout.isReservedField(key)) {
                        field(key, entry.getValue());
                    }
                }
            }
            return this;
        }

        public SmartPoint build() {
            return new SmartPoint(this);
        }
    }
}
