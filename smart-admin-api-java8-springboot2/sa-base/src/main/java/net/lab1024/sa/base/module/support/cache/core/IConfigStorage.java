package net.lab1024.sa.base.module.support.cache.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 配置存储 — 组合 IHashCache，绑定固定 key。
 * getConfig(key) 返回 Value 包装器，支持链式类型转换。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public interface IConfigStorage {

    String getKey();

    /** 获取单个配置值 */
    Value getConfig(String field);

    /** 获取配置值，为空时使用提供者生成并自动写入 */
    Value getConfig(String field, Supplier<String> defaultValueSupplier);

    /** 批量获取配置值 */
    List<Value> getConfigs(String... fields);

    /** 批量获取配置值 */
    List<Value> getConfigs(Collection<String> fields);

    /** 设置单个配置 */
    Boolean setConfig(String field, Object value);

    /** 批量设置配置 */
    Boolean setConfigs(Map<String, Object> values);

    /** 删除配置项 */
    Boolean remove(String... fields);

    /** 获取并删除配置（原子操作） */
    Value getAndRemove(String field);

    /** 检查配置是否存在 */
    Boolean hasConfig(String field);

    /** 获取所有配置 */
    Map<String, Value> getAll();

    /** 获取配置项数量 */
    long size();

    /** 清空所有配置 */
    Boolean clear();
}
