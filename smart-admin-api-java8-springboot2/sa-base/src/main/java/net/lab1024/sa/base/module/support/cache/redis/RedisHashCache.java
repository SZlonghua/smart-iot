package net.lab1024.sa.base.module.support.cache.redis;

import net.lab1024.sa.base.module.support.cache.core.IHashCache;
import net.lab1024.sa.base.module.support.cache.core.Value;
import org.springframework.data.redis.core.HashOperations;

import java.util.*;
import java.util.stream.Collectors;

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
    public List<Value> multiGet(Collection<String> fields) {
        return hashOps.multiGet(key, new ArrayList<>(fields))
                .stream()
                .map(SimpleValue::of)
                .collect(Collectors.toList());
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
        return result > 0;
    }

    @Override
    public Boolean delete(String... fields) {
        return hashOps.delete(key, (Object[]) fields) > 0;
    }

    @Override
    public Boolean exists(String field) {
        return hashOps.hasKey(key, field);
    }

    @Override
    public Set<Value> fields() {
        return hashOps.keys(key)
                .stream()
                .map(SimpleValue::of)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Map<String, Value> getAll() {
        return hashOps.entries(key)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> SimpleValue.of(e.getValue()),
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    @Override
    public long size() {
        return hashOps.size(key);
    }
}
