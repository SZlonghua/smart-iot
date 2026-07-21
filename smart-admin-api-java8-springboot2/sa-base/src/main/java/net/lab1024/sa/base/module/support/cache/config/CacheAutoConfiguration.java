package net.lab1024.sa.base.module.support.cache.config;

import net.lab1024.sa.base.module.support.cache.core.ICacheManager;
import net.lab1024.sa.base.module.support.cache.core.IConfigStorageManager;
import net.lab1024.sa.base.module.support.cache.core.IStringCache;
import net.lab1024.sa.base.module.support.cache.redis.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import javax.annotation.Resource;

/**
 * 缓存自动装配。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
@Configuration
public class CacheAutoConfiguration {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private HashOperations<String, String, Object> hashOperations;

    @Resource
    private SetOperations<String, Object> setOperations;

    @Bean
    public IStringCache stringCacheOperation() {
        return new RedisStringCache(redisTemplate);
    }

    @Bean
    public ICacheManager cacheOperationManager() {
        return new RedisCacheManager(stringCacheOperation(), hashOperations, setOperations);
    }

    @Bean
    public IConfigStorageManager configStorageManager() {
        return new RedisConfigStorageManager(hashOperations);
    }
}
