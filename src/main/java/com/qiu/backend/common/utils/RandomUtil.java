package com.qiu.backend.common.utils;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

    /**
     * 生成指定位数的随机数字
     */
    public static String randomNumbers(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成指定位数的随机字母（大小写）
     */
    public static String randomLetters(int length) {
        if (length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            char c = (char) (random.nextBoolean() ?
                    'A' + random.nextInt(26) :
                    'a' + random.nextInt(26));
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 生成指定位数的字母数字混合字符串
     */
    public static String randomAlphanumeric(int length) {
        if (length <= 0) {
            return "";
        }
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成简化的UUID（无横线）
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成随机密码（含特殊字符）
     */
    public static String randomPassword(int length) {
        if (length <= 0) {
            return "";
        }
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()";
        Random random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 从指定字符集中生成随机字符串
     */
    public static String randomFromChars(int length, String chars) {
        if (length <= 0 || chars == null || chars.isEmpty()) {
            return "";
        }
        Random random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}