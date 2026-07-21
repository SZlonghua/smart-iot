package net.lab1024.sa.base.module.support.cache.redis;

import net.lab1024.sa.base.module.support.cache.core.ICacheManager;
import net.lab1024.sa.base.module.support.cache.core.IHashCache;
import net.lab1024.sa.base.module.support.cache.core.ISetCache;
import net.lab1024.sa.base.module.support.cache.core.IStringCache;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 缓存管理器 — StringCache 单例，HashCache/SetCache 按 key 缓存复用。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public class RedisCacheManager implements ICacheManager {

    private final IStringCache stringCache;
    private final HashOperations<String, String, Object> hashOps;
    private final SetOperations<String, Object> setOps;

    private final Map<String, IHashCache> hashCacheMap = new ConcurrentHashMap<>();
    private final Map<String, ISetCache> setCacheMap = new ConcurrentHashMap<>();

    public RedisCacheManager(IStringCache stringCache,
                              HashOperations<String, String, Object> hashOps,
                              SetOperations<String, Object> setOps) {
        this.stringCache = stringCache;
        this.hashOps = hashOps;
        this.setOps = setOps;
    }

    @Override
    public IStringCache getStringCache() {
        return stringCache;
    }

    @Override
    public IHashCache getHashCache(String key) {
        return hashCacheMap.computeIfAbsent(key, k -> new RedisHashCache(k, hashOps));
    }

    @Override
    public ISetCache getSetCache(String key) {
        return setCacheMap.computeIfAbsent(key, k -> new RedisSetCache(k, setOps));
    }
}
