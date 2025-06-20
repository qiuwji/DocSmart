package com.qiu.backend.core.result;

import com.qiu.backend.common.core.Result.Result;
import com.qiu.backend.common.core.Result.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResultTest {
    @Test
    void testSuccess() {
        Result<String> result = Result.success("data");
        assertEquals(200, result.getCode());
        assertEquals("data", result.getData());
    }

    @Test
    void testFailedWithEnum() {
        Result<Object> result = Result.failed(ResultCode.UNAUTHORIZED);
        assertEquals(401, result.getCode());
        assertEquals("暂未登录或token已过期", result.getMessage());
    }
}
