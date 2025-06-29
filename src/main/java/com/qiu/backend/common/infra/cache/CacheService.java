package com.qiu.backend.common.infra.cache;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 通用缓存接口
 */
public interface CacheService {
    /**
     * 设置缓存 带过期时间
     */
    void set(String key, Object value, long expireSeconds);

    /**
     * 设置缓存 带过期时间，单位是int
     */
    void set(String key, Object value, int expireSeconds);

    /**
     * 带单位的
     */
    void set(String key, Object value, long expireSeconds, TimeUnit unit);

    /**
     * 设置缓存 不带过期时间
     */
    void set(String key, Object value);


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
     * 设置过期时间，带时间单位
     */
    boolean expire(String key, long expireSeconds, TimeUnit unit);

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

    /**
     * 根据 Redis 的 key 模式，查询所有匹配的 key
     * @param pattern 比如 "docs:upload:in_progress:*"
     * @return 匹配到的 key 集合
     */
    Set<String> keys(String pattern);

    /*---------------------链表操作------------------------------------------*/
    /**
     * 向列表右侧推入元素
     */
    void rightPush(String key, String value);

    /**
     * 获取列表中指定范围的元素
     */
    List<String> range(String key, long start, long end);

    /**
     * 获取整个列表所有元素
     */
    default List<String> getAllFromList(String key) {
        return range(key, 0, -1);
    }

    /**
     * 判断某个元素是否在列表中
     */
    boolean listContains(String key, String value);

    /**
     * 删除列表中的指定元素
     */
    void removeFromList(String key, String value);

    /**
     * 获取列表长度
     */
    long listSize(String key);

}
