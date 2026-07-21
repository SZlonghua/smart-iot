package net.lab1024.sa.base.module.support.cache.core;

/**
 * 缓存管理器 — String 不绑 key，Hash/Set 绑 key。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public interface ICacheManager {

    IStringCache getStringCache();

    IHashCache getHashCache(String key);

    ISetCache getSetCache(String key);
}
