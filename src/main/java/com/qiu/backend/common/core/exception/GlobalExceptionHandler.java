package com.qiu.backend.common.core.exception;

import com.qiu.backend.common.core.Result.Result;
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

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Object> handleValidException(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return Result.failed(message);
    }

    @ExceptionHandler(value = Exception.class)
    public Result<Object> handleException(Exception e) {
        return Result.failed(e.getMessage());
    }
}
