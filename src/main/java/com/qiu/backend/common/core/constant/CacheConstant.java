package com.qiu.backend.common.core.constant;

public class CacheConstant {

    // 键不能为空
    public static final String KEY_CANNOT_BE_EMPTY = "键不能为空";

    // 值不能为空
    public static final String VALUE_CANNOT_BE_EMPTY = "值不能为空";

    // 键或值不能为空
    public static final String KEY_OR_VALUE_CANNOT_BE_EMPTY = "键或值不能为空";

    // clazz不能为空
    public static final String CLAZZ_CANNOT_BE_EMPTY = "clazz不能为空";

    // 过期时间必须大于0
    public static final String EXPIRED_TIME_MUST_BIGGER_THAN_ZERO = "过期时间必须大于0";

    // 类型不匹配
    public static final String TYPE_CONFLICT = "类型不匹配";

    // --------------------------------------------------------------------------

    // 验证码重发冷却时间前缀（单位：秒/毫秒，根据实际情况）
    public static final String CAPTCHA_COOLDOWN_PREFIX = "captcha:cooldown:";

    // 验证码重发时间限制
    public static final int COOLDOWN_TIME = 60;

    // 验证码有效
    public static final String CAPTCHA_VALID_PREFIX = "captcha:valid:";

    // 验证码有效时间：5分钟
    public static final int VALID_TIME = 60 * 5;
}
