package com.qiu.backend.core.util;

import com.qiu.backend.common.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JwtUtilTest {

    @Test
    public void testJwtUtil() {
        String s = JwtUtil.generateToken(1L);

        assertNotNull(s);

        System.out.println(s);

        boolean b = JwtUtil.validateToken(s);

        assertTrue(b);

        Long id = JwtUtil.getUserIdFromToken(s);

        assertEquals(1L, id);
    }
}
