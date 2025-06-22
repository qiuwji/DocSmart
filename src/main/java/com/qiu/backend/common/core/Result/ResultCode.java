package com.qiu.backend.common.core.Result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(400, "参数校验失败"),
    EMAIL_FORMAT_ERROR(400, "邮箱格式错误"),
    REGISTERED(400, "已被注册"),
    UNAUTHORIZED(401, "暂未登录或token已过期"),
    EMAIL_OR_PASSWORD_FAILED(401, "账号或密码错误"),
    COOLDOWN_FAILED(429, "操作处于冷却期，请稍后再试"),
    FORBIDDEN(403, "没有相关权限");

    private final long code;
    private final String message;
}
