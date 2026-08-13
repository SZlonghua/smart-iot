package net.lab1024.sa.base.common.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map 构建工具 — 链式 put，自动跳过 null 值。
 *
 * <pre>{@code
 * Map<String, Object> map = SmartMapUtil.builder()
 *         .put("key1", value1)
 *         .put("key2", nullableValue)   // null 自动跳过
 *         .put("key3", value3)
 *         .build();
 * }</pre>
 *
 * @Author 廖涛
 * @Date 2026/07/25
 * @Copyright 1024创新实验室
 */
public final class SmartMapUtil {

    private SmartMapUtil() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, Object> map = new LinkedHashMap<>();

        /** 添加键值对，value 为 null 时自动跳过 */
        public Builder put(String key, Object value) {
            if (value != null) {
                map.put(key, value);
            }
            return this;
        }

        /** 添加键值对（即使 value 为 null 也写入） */
        public Builder putNullable(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public Map<String, Object> build() {
            return map;
        }
    }
}
