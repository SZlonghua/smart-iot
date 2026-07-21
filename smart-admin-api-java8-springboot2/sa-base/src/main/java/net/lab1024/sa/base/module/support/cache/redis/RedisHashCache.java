package net.lab1024.sa.base.module.support.cache.redis;

import net.lab1024.sa.base.module.support.cache.core.IHashCache;
import net.lab1024.sa.base.module.support.cache.core.Value;
import org.springframework.data.redis.core.HashOperations;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis Hash 缓存实现。构造时绑定 key，操作只传 field。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public class RedisHashCache implements IHashCache {

    private final String key;
    private final HashOperations<String, String, Object> hashOps;

    public RedisHashCache(String key, HashOperations<String, String, Object> hashOps) {
        this.key = key;
        this.hashOps = hashOps;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Value get(String field) {
        return SimpleValue.of(hashOps.get(key, field));
    }

    @Override
    public Boolean set(String field, Object value) {
        hashOps.put(key, field, value);
        return true;
    }

    @Override
    public Boolean setAll(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return false;
        }
        hashOps.putAll(key, map);
        return true;
    }

    @Override
    public Boolean delete(String field) {
        Long result = hashOps.delete(key, field);
        return result != null && result > 0;
    }

    @Override
    public Boolean delete(String... fields) {
        Long result = hashOps.delete(key, (Object[]) fields);
        return result != null && result > 0;
    }

    @Override
    public Boolean exists(String field) {
        return hashOps.hasKey(key, field);
    }

    @Override
    public Set<Value> fields() {
        Set<String> keys = hashOps.keys(key);
        Set<Value> result = new java.util.LinkedHashSet<>();
        if (keys != null) {
            for (String k : keys) {
                result.add(SimpleValue.of(k));
            }
        }
        return result;
    }

    @Override
    public Map<String, Value> getAll() {
        Map<String, Object> all = hashOps.entries(key);
        Map<String, Value> result = new LinkedHashMap<>();
        if (all != null) {
            all.forEach((k, v) -> result.put(k, SimpleValue.of(v)));
        }
        return result;
    }

    @Override
    public long size() {
        Long size = hashOps.size(key);
        return size == null ? 0 : size;
    }
}
