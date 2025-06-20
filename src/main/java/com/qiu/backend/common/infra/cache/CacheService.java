package com.qiu.backend.common.infra.cache;

/**
 * 通用缓存接口
 */
public interface CacheService {
    /**
     * 设置缓存
     */
    void set(String key, Object value, long expireSeconds);

    /**
     * 获取缓存
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     */
    boolean delete(String key);

    /**
     * 检查key是否存在
     */
    boolean exists(String key);

    /**
     * 设置过期时间
     */
    boolean expire(String key, long expireSeconds);

    /**
     * 获取剩余过期时间
     */
    long getExpire(String key);

    /**
     * 递增
     */
    long increment(String key, long delta);

    /**
     * 递减
     */
    long decrement(String key, long delta);
}
