package net.lab1024.sa.base.module.support.cache.core;

/**
 * String 结构缓存 — 对应 Redis String。操作直接传入 key。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public interface IStringCache extends ICache {

    String get(String key);

    <T> T getObject(String key, Class<T> clazz);

    void set(String key, String value);

    void set(String key, String value, long seconds);

    void setObject(String key, Object value);

    void setObject(String key, Object value, long seconds);

    boolean delete(String key);

    boolean exists(String key);

    boolean expire(String key, long seconds);

    long getExpire(String key);

    long increment(String key, long delta);
}
