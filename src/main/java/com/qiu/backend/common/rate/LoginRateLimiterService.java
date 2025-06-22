package com.qiu.backend.common.rate;

import com.qiu.backend.common.infra.cache.impl.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginRateLimiterService {

    private static final String PREFIX = "login_limit:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = Duration.ofMinutes(10).getSeconds();

    private final RedisCacheService cacheService;

    @Autowired
    public LoginRateLimiterService(RedisCacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * 尝试申请登录许可
     * @param deviceId 设备ID
     * @param email 邮箱
     * @return 是否允许登录
     */
    public boolean tryAcquire(String deviceId, String email) {
        String key = generateKey(deviceId, email);

        // 递增计数
        long count = cacheService.increment(key, 1);

        // 如果是第一次访问，设置过期时间
        if (count == 1) {
            cacheService.expire(key, WINDOW_SECONDS);
        }

        // 是否允许登录尝试（次数 <= 最大限制）
        return count <= MAX_ATTEMPTS;
    }

    /**
     * 登录成功后重置计数
     */
    public void reset(String deviceId, String email) {
        String key = PREFIX + deviceId + ":" + email;
        cacheService.delete(key);
    }

    /**
     * 生成key
     */
    public String generateKey(String deviceId, String email) {
        return PREFIX + deviceId + ":" + email;
    }
}
