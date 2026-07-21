package net.lab1024.sa.base.module.support.cache.redis;

import net.lab1024.sa.base.module.support.cache.core.ISetCache;
import org.springframework.data.redis.core.SetOperations;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Redis Set 缓存实现。构造时绑定 key，操作只传 member。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public class RedisSetCache implements ISetCache {

    private final String key;
    private final SetOperations<String, Object> setOps;

    public RedisSetCache(String key, SetOperations<String, Object> setOps) {
        this.key = key;
        this.setOps = setOps;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean add(String... members) {
        Long result = setOps.add(key, (Object[]) members);
        return result != null && result > 0;
    }

    @Override
    public boolean remove(String... members) {
        Long result = setOps.remove(key, (Object[]) members);
        return result != null && result > 0;
    }

    @Override
    public boolean contains(String member) {
        return Boolean.TRUE.equals(setOps.isMember(key, member));
    }

    @Override
    public Set<String> members() {
        Set<Object> members = setOps.members(key);
        if (members == null) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>();
        for (Object m : members) {
            result.add(m == null ? null : m.toString());
        }
        return result;
    }

    @Override
    public long size() {
        Long size = setOps.size(key);
        return size == null ? 0 : size;
    }
}
