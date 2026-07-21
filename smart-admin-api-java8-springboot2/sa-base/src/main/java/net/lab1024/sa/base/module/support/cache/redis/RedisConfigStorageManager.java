package net.lab1024.sa.base.module.support.cache.redis;

import net.lab1024.sa.base.module.support.cache.core.IConfigStorage;
import net.lab1024.sa.base.module.support.cache.core.IConfigStorageManager;
import org.springframework.data.redis.core.HashOperations;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 配置存储管理器实现。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public class RedisConfigStorageManager implements IConfigStorageManager {

    private final HashOperations<String, String, Object> hashOps;
    private final Map<String, IConfigStorage> storageMap = new ConcurrentHashMap<>();

    public RedisConfigStorageManager(HashOperations<String, String, Object> hashOps) {
        this.hashOps = hashOps;
    }

    @Override
    public IConfigStorage getStorage(String key) {
        return storageMap.computeIfAbsent(key, k -> {
            RedisHashCache hashCache = new RedisHashCache(k, hashOps);
            return new RedisConfigStorage(hashCache);
        });
    }

    @Override
    public boolean removeStorage(String key) {
        IConfigStorage storage = storageMap.remove(key);
        if (storage != null) {
            return storage.clear();
        }
        return false;
    }

    @Override
    public Set<String> getKeys() {
        return storageMap.keySet();
    }
}
