package net.lab1024.sa.base.module.support.cache.redis;

import com.alibaba.fastjson.JSON;
import net.lab1024.sa.base.module.support.cache.core.Value;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Value 默认实现，raw 为 Object，支持 Number/Boolean 原生类型。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public class SimpleValue implements Value {

    private final Object raw;

    private SimpleValue(Object raw) {
        this.raw = raw;
    }

    public static SimpleValue of(Object raw) {
        return new SimpleValue(raw);
    }

    public static SimpleValue empty() {
        return new SimpleValue(null);
    }

    @Override
    public String get() {
        return raw == null ? null : raw.toString();
    }

    @Override
    public String asString() {
        return get();
    }

    @Override
    public int asInt() {
        return asInt(0);
    }

    @Override
    public int asInt(int defaultValue) {
        if (raw == null) {
            return defaultValue;
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        try {
            return Integer.parseInt(raw.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public long asLong() {
        return asLong(0L);
    }

    @Override
    public long asLong(long defaultValue) {
        if (raw == null) {
            return defaultValue;
        }
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public double asDouble() {
        return asDouble(0.0);
    }

    @Override
    public double asDouble(double defaultValue) {
        if (raw == null) {
            return defaultValue;
        }
        if (raw instanceof Number) {
            return ((Number) raw).doubleValue();
        }
        try {
            return Double.parseDouble(raw.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean asBoolean() {
        return asBoolean(false);
    }

    @Override
    public boolean asBoolean(boolean defaultValue) {
        if (raw == null) {
            return defaultValue;
        }
        if (raw instanceof Boolean) {
            return (Boolean) raw;
        }
        String lower = raw.toString().toLowerCase();
        return "true".equals(lower) || "1".equals(lower) || "yes".equals(lower);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T asObject(Class<T> clazz) {
        if (raw == null) {
            return null;
        }
        if (clazz.isInstance(raw)) {
            return (T) raw;
        }
        // Jackson 反序列化 Hash value 时可能返回 LinkedHashMap（类型信息丢失），
        // 走 JSON 序列化再反序列化还原。raw.toString() 是 Java 格式 {k=v} 而非 JSON {"k":"v"}，故用 toJSONString。
        if (raw instanceof Map) {
            return JSON.parseObject(JSON.toJSONString(raw), clazz);
        }
        // 其他未知类型同样先转 JSON 再解析
        return JSON.parseObject(JSON.toJSONString(raw), clazz);
    }

    @Override
    public boolean isPresent() {
        return raw != null;
    }

    @Override
    public String orElse(String defaultValue) {
        return raw != null ? get() : defaultValue;
    }

    @Override
    public String orElseGet(Supplier<String> defaultValueSupplier) {
        return raw != null ? get() : defaultValueSupplier.get();
    }
}
