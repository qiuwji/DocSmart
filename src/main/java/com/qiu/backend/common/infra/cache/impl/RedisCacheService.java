package com.qiu.backend.common.infra.cache.impl;

import com.qiu.backend.common.infra.cache.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.qiu.backend.common.core.constant.CacheConstant.*;

@Component
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void set(String key, Object value, long expireSeconds) {
        set(key, value, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void set(String key, Object value, int expireSeconds) {
        set(key, value, (long) expireSeconds);
    }

    @Override
    public void set(String key, Object value, long expireSeconds, TimeUnit unit) {
        if (key == null || key.isEmpty()
                || value == null) {
            throw new IllegalArgumentException(KEY_OR_VALUE_CANNOT_BE_EMPTY);
        }

        if (expireSeconds <= 0) {
            throw new IllegalArgumentException(EXPIRED_TIME_MUST_BIGGER_THAN_ZERO);
        }

        redisTemplate.opsForValue().set(key, value, expireSeconds, unit);
    }

    @Override
    public void set(String key, Object value) {
        if (key == null || key.isEmpty()
                || value == null) {
            throw new IllegalArgumentException(KEY_OR_VALUE_CANNOT_BE_EMPTY);
        }

        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException(KEY_CANNOT_BE_EMPTY);
        }

        if (clazz == null) {
            throw new IllegalArgumentException(CLAZZ_CANNOT_BE_EMPTY);
        }

        try {
            Object value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                return null;
            }

            // 处理Integer到Long的转换
            if (clazz == Long.class && value instanceof Integer) {
                return clazz.cast(((Integer) value).longValue());
            }

            // 类型检查
            if (clazz.isAssignableFrom(value.getClass())) {
                return clazz.cast(value);
            }
            else {
                throw new Exception(TYPE_CONFLICT);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("获取缓存失败", e);
        }
    }

    @Override
    public boolean delete(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException(KEY_CANNOT_BE_EMPTY);
        }

        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    @Override
    public boolean exists(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException(KEY_CANNOT_BE_EMPTY);
        }

        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public boolean expire(String key, long expireSeconds) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("键不能为空");
        }
        if (expireSeconds <= 0) {
            return Boolean.TRUE.equals(redisTemplate.persist(key)); // 移除过期时间（永久有效）
        }
        return Boolean.TRUE.equals(
                redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS)
        );
    }

    /*
     返回-1表示键存在但是没有设置过期时间
     返回-2表示键不存在
     */
    @Override
    public long getExpire(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("键不能为空");
        }
        return Optional.ofNullable(
                redisTemplate.getExpire(key, TimeUnit.SECONDS)
        ).orElse(-2L); // 默认返回-2（键不存在）
    }

    @Override
    public long increment(String key, long delta) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("键不能为空");
        }
        if (delta < 0) {
            throw new IllegalArgumentException("递增步长必须大于等于0");
        }

        try {
            return Optional.ofNullable(
                    redisTemplate.opsForValue().increment(key, delta)
            ).orElse(0L);
        } catch (RedisSystemException e) {
            throw new IllegalArgumentException("键" + key + "值不是数字类型，无法递增");
        }
    }

    @Override
    public long decrement(String key, long delta) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("键不能为空");
        }
        if (delta < 0) {
            throw new IllegalArgumentException("递减步长必须大于等于0");
        }

        try {
            return Optional.ofNullable(
                    redisTemplate.opsForValue().decrement(key, delta)
            ).orElse(0L);
        } catch (RedisSystemException e) {
            throw new IllegalArgumentException("键" + key + "值不是数字类型，无法递增");
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        // 直接调用 RedisTemplate.keys，适用于小规模 key 空间
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys : Collections.emptySet();
    }
}
