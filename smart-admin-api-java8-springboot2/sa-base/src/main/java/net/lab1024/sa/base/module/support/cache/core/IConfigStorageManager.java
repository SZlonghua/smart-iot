package net.lab1024.sa.base.module.support.cache.core;

import java.util.Set;

/**
 * 配置存储管理器 — 管理多个配置 key，每个 key 对应一个 IConfigStorage。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public interface IConfigStorageManager {

    IConfigStorage getStorage(String key);

    boolean removeStorage(String key);

    Set<String> getKeys();
}
