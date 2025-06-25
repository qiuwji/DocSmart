package com.qiu.backend.common.core.exception;

import com.qiu.backend.common.core.Result.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ApiException.class)
    public Result<Object> handleApiException(ApiException e) {
        return Result.failed(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleValidException(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        Result<Object> body = Result.failed(message);
        return ResponseEntity
                .badRequest()      // HTTP 400
                .body(body);
    }

    @ExceptionHandler(value = Exception.class)
    public Result<Object> handleException(Exception e) throws Exception {
        // 如果是 Spring Security 的异常，直接抛出，不处理
        if (e instanceof org.springframework.security.core.AuthenticationException ||
                e instanceof org.springframework.security.access.AccessDeniedException) {
            throw e; // 让 Security 的异常处理器处理
        }
        return Result.failed(e.getMessage());
    }

    @ExceptionHandler(value = BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        // 将 data 作为错误消息的一部分
        String message = e.getErrorCode().getMessage();
        if (e.getData() != null) {
            message += " | 数据：" + e.getData();
        }
        return Result.failed(message);
    }
}
