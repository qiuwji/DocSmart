package com.qiu.backend.core.infra;

import com.qiu.backend.common.infra.cache.impl.RedisCacheService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisCacheServiceTest {

    @Autowired
    private RedisCacheService redisCacheService;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class Inner {
        String a;
        String b;
    }

    @Test
    public void testSetWithExpireTime() {
        redisCacheService.set("key", "value", 3);
        String s = redisCacheService.get("key", String.class);
        assertEquals(s, "value");
        try {
            Thread.sleep(3 * 1000);
            assertFalse(redisCacheService.exists("key"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDelete() {
        Inner inner = new Inner("Qiu", "Ji");
        redisCacheService.set("name", inner);

        Inner inner1 = redisCacheService.get("name", Inner.class);

        assertEquals(inner.a, inner1.a);
        assertEquals(inner.b, inner1.b);

        redisCacheService.delete("name");
        assertFalse(redisCacheService.exists("name"));
    }

    @Test
    public void testIncrementAndDecrement() {
        String key = "number";
        redisCacheService.set(key, 10);
        // 尝试+2
        redisCacheService.increment(key, 2);
        assertEquals(redisCacheService.get(key, Integer.class), 12);

        // 尝试-5
        redisCacheService.decrement(key, 5);
        assertEquals(redisCacheService.get(key, Integer.class), 7);

        assertThrows(IllegalArgumentException.class, () -> {
           redisCacheService.increment(null, 2);
        });

        String invalidKey = "key";
        redisCacheService.set(invalidKey, "Love and Peace");

        assertThrows(IllegalArgumentException.class, () -> {
            redisCacheService.increment(null, 2);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            redisCacheService.increment(invalidKey, 2);
        });

        redisCacheService.delete(key);
        redisCacheService.delete(invalidKey);
    }
}
