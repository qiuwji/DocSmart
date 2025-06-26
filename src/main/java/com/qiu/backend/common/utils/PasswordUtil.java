package com.qiu.backend.common.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码工具类（基于 BCrypt，自动管理盐值）
 */
public class PasswordUtil {

    /**
     * 加密密码（自动生成盐值并嵌入到哈希结果中）
     * @param plainPassword 明文密码
     * @return 加密后的哈希字符串（包含盐值）
     */
    public static String encrypt(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * 校验密码
     * @param plainPassword 用户输入的明文密码
     * @param hashedPassword 数据库存储的哈希密码
     * @return 是否匹配
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

}