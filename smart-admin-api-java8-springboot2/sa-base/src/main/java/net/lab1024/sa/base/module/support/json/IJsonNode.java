package net.lab1024.sa.base.module.support.json;

/**
 * JSON 节点接口 —— 外部唯一接触面，完全屏蔽 Jackson
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public interface IJsonNode {

    // ===== 读取 =====
    String getString(String field);

    Integer getInt(String field);

    Double getDouble(String field);

    Boolean getBoolean(String field);

    Long getLong(String field);

    IJsonNode get(String field);

    // ===== 数组 =====
    boolean isArray();

    int size();

    IJsonNode get(int index);

    // ===== 写入 =====
    void put(String field, String value);

    void put(String field, int value);

    void put(String field, double value);

    void put(String field, boolean value);

    void put(String field, long value);

    void put(String field, IJsonNode value);

    void add(IJsonNode value);

    // ===== 基本 =====
    String asText();

    boolean isNull();

    boolean has(String field);
}
