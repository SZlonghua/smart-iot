package net.lab1024.sa.base.module.support.cache.core;

import java.util.Set;

/**
 * Set 结构缓存 — 对应 Redis Set。创建时绑定 key，操作只传 member。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public interface ISetCache extends ICache {

    /** 此 Set 绑定的 key */
    String getKey();

    boolean add(String... members);

    boolean remove(String... members);

    boolean contains(String member);

    Set<String> members();

    long size();
}
