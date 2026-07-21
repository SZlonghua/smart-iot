package net.lab1024.sa.base.module.support.cache.core;

import java.util.Map;
import java.util.Set;

/**
 * Hash 结构缓存 — 对应 Redis Hash。创建时绑定 key，操作只传 field。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public interface IHashCache extends ICache {

    /** 此 Hash 绑定的 key */
    String getKey();

    Value get(String field);

    Boolean set(String field, Object value);

    Boolean setAll(Map<String, Object> map);

    Boolean delete(String field);

    Boolean delete(String... fields);

    Boolean exists(String field);

    Set<Value> fields();

    Map<String,Value> getAll();

    long size();
}
