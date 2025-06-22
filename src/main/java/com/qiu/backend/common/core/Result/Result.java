package com.qiu.backend.common.core.Result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class Result<T> implements Serializable {
    /**
     *  状态码
     */
    private long code;

    /**
     * 消息
     */
    private String message;

    /**
     * 返回值
     */
    private T data;

    /**
     * 成功方法
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 无data的success
     */
    public static Result success() {
        return new Result<>(ResultCode.SUCCESS.getCode(),
                ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 失败方法 支持自定义错误码
     */
    public static <T> Result<T> failed(IErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败方法 支持自定义消息
     */
    public static <T> Result<T> failed(String message) {
        return new Result<>(ResultCode.FAILED.getCode(), message, null);
    }

}
