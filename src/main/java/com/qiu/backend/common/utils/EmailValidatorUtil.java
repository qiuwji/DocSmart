package com.qiu.backend.common.utils;

import java.util.regex.Pattern;

public class EmailValidatorUtil {
    // 基础正则：允许大多数常见邮箱格式（如 user@example.com）
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);

    public static boolean isValid(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return pattern.matcher(email).matches();
    }
}
