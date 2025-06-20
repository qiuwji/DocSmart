package com.qiu.backend.core.exception;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiExceptionTest {
    @Test
    void testException() {
        ApiException ex = new ApiException(ResultCode.FORBIDDEN);
        assertEquals(403, ex.getErrorCode().getCode());
    }
}
