package net.lab1024.sa.base.module.support.cache.redis;

import net.lab1024.sa.base.module.support.cache.core.IConfigStorage;
import net.lab1024.sa.base.module.support.cache.core.IHashCache;
import net.lab1024.sa.base.module.support.cache.core.Value;

import java.util.*;
import java.util.function.Supplier;

/**
 * Redis 配置存储实现 — 组合 IHashCache，绑定固定 key。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public class RedisConfigStorage implements IConfigStorage {

    private final IHashCache hashCache;

    public RedisConfigStorage(IHashCache hashCache) {
        this.hashCache = hashCache;
    }

    @Override
    public String getKey() {
        return hashCache.getKey();
    }

    @Override
    public Value getConfig(String field) {
        return hashCache.get(field);
    }

    @Override
    public Value getConfig(String field, Supplier<String> defaultValueSupplier) {
        Value value = hashCache.get(field);
        if (!value.isPresent()) {
            String defaultValue = defaultValueSupplier.get();
            hashCache.set(field, defaultValue);
            return SimpleValue.of(defaultValue);
        }
        return value;
    }

    @Override
    public List<Value> getConfigs(String... fields) {
        return getConfigs(Arrays.asList(fields));
    }

    @Override
    public List<Value> getConfigs(Collection<String> fields) {
        List<Value> result = new ArrayList<>(fields.size());
        for (String field : fields) {
            result.add(hashCache.get(field));
        }
        return result;
    }

    @Override
    public Boolean setConfig(String field, Object value) {
        return hashCache.set(field, value);
    }

    @Override
    public Boolean setConfigs(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        return hashCache.setAll(values);
    }

    @Override
    public Boolean remove(String... fields) {
        return hashCache.delete(fields);
    }

    @Override
    public Value getAndRemove(String field) {
        Value value = hashCache.get(field);
        hashCache.delete(field);  // 单 field 删除
        return value;
    }

    @Override
    public Boolean hasConfig(String field) {
        return hashCache.exists(field);
    }

    @Override
    public Map<String, Value> getAll() {
        return hashCache.getAll();
    }

    @Override
    public long size() {
        return hashCache.size();
    }

    @Override
    public Boolean clear() {
        Set<Value> fields = hashCache.fields();
        if (fields.isEmpty()) {
            return false;
        }
        return hashCache.delete(fields.stream().map(Value::get).toArray(String[]::new));
    }
}
