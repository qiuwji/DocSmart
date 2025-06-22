package com.qiu.backend.core.util;

import com.qiu.backend.common.utils.PasswordUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    void encrypt_ShouldGenerateDifferentHashesForSamePassword() {
        String password = "Test123!";
        String hash1 = PasswordUtil.encrypt(password);
        String hash2 = PasswordUtil.encrypt(password);

        // 相同密码每次加密结果不同（因为自动使用不同盐值）
        assertNotEquals(hash1, hash2);
    }

    @Test
    void verify_ShouldReturnTrueForCorrectPassword() {
        String password = "MySecurePwd456!";
        String hashedPassword = PasswordUtil.encrypt(password);
        assertTrue(PasswordUtil.verify(password, hashedPassword));
    }

    @Test
    void verify_ShouldReturnFalseForIncorrectPassword() {
        String hashedPassword = PasswordUtil.encrypt("CorrectPwd");
        assertFalse(PasswordUtil.verify("WrongPwd", hashedPassword));
    }

    @Test
    void encrypt_ShouldThrowExceptionForNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.encrypt(null));
    }
}