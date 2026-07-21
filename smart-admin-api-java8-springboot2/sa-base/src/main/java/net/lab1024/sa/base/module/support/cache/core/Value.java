package net.lab1024.sa.base.module.support.cache.core;

import java.util.function.Supplier;

/**
 * 配置值包装器 — 链式类型转换，屏蔽原始字符串。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public interface Value {

    /** 获取原始字符串值 */
    String asString();

    /** 转换为 int，null 或非数字返回 0 */
    int asInt();

    /** 转换为 int，null 或非数字返回默认值 */
    int asInt(int defaultValue);

    /** 转换为 long */
    long asLong();

    long asLong(long defaultValue);

    /** 转换为 double */
    double asDouble();

    double asDouble(double defaultValue);

    /** 转换为 boolean（"true"/"1"/"yes" → true） */
    boolean asBoolean();

    boolean asBoolean(boolean defaultValue);

    /** 反序列化为对象 */
    <T> T asObject(Class<T> clazz);

    /** 是否有值（非 null） */
    boolean isPresent();

    /** 如果为空则返回默认值 */
    String orElse(String defaultValue);

    /** 如果为空则使用提供者生成默认值 */
    String orElseGet(Supplier<String> defaultValueSupplier);

    /** 原始值，可能为 null */
    String get();
}
