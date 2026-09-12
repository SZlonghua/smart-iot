package net.lab1024.sa.base.storage.model;

import java.util.Map;

/**
 * 时序查询行读取器 — 通道查询返回行（Map）的包装：取值转换（{@link #getString}/{@link #getLong}/{@link #get}）
 * 内聚一处，解码处不再出现裸 get 与散落的类型转换。
 * <p>
 * 行键即写侧列名（下划线小写一套命名，常量单一来源见
 * {@link net.lab1024.sa.base.storage.timeseries.TimeseriesLayout}，本类不重复声明）；
 * 动态属性列名由调用方经 {@link net.lab1024.sa.base.storage.timeseries.TimeseriesLayout#rowKey} 归一化后传入。
 *
 * @Author 廖涛
 * @Date 2026/09/10
 * @Copyright 1024创新实验室
 */
public final class TimeseriesRow {

    /** 查询行（通道返回 Map，键为列名下划线小写形态） */
    private final Map<String, Object> row;

    public TimeseriesRow(Map<String, Object> row) {
        this.row = row;
    }

    /** 列值 → String（NCHAR 列驱动返回 String，其余类型容错转换） */
    public String getString(String key) {
        Object value = row.get(key);
        return value == null ? null : String.valueOf(value);
    }

    /** 列值 → long（时间列通道已归一为毫秒 long：消息时间列 create_time / 主时间列 _ts） */
    public long getLong(String key) {
        Object value = row.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return value == null ? 0L : Long.parseLong(String.valueOf(value));
    }

    /** 原始列值（动态属性列、column 单值列扫描等类型未知场景） */
    public Object get(String key) {
        return row.get(key);
    }

    /** 原始行 Map 视图（row 形态属性摊平：整行拷贝后剔保留键的场景） */
    public Map<String, Object> asMap() {
        return row;
    }
}
