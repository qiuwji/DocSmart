package com.qiu.backend.modules.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.qiu.backend.common.core.constant.RoleConstant;
import com.qiu.backend.common.core.constant.SceneConstant;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.common.utils.JwtUtil;
import com.qiu.backend.common.utils.PasswordUtil;
import com.qiu.backend.modules.auth.service.impl.AuthServiceImpl;
import com.qiu.backend.modules.model.dto.GetEmailCaptchaDTO;
import com.qiu.backend.modules.model.dto.LoginDTO;
import com.qiu.backend.modules.model.dto.RegistrationDTO;
import com.qiu.backend.modules.model.entity.User;
import com.qiu.backend.modules.model.entity.UserRole;
import com.qiu.backend.modules.model.vo.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

import com.qiu.backend.common.infra.cache.impl.RedisCacheService;
import com.qiu.backend.common.rate.LoginRateLimiterService;
import com.qiu.backend.modules.auth.service.MailService;
import com.qiu.backend.modules.user.mapper.UserMapper;
import com.qiu.backend.modules.user.service.UserRoleService;
import com.qiu.backend.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private RedisCacheService cacheService;

    @Mock
    private MailService mailService;

    @Mock
    private UserService userService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private LoginRateLimiterService loginRateLimiterService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthServiceImpl authService;

    private GetEmailCaptchaDTO validCaptchaDTO;
    private RegistrationDTO validRegistrationDTO;
    private LoginDTO validLoginDTO;

    @BeforeEach
    void setUp() {
        validCaptchaDTO = new GetEmailCaptchaDTO();
        validCaptchaDTO.setEmail("test@example.com");
        validCaptchaDTO.setScene(SceneConstant.REGISTER_CONSTANT);

        validRegistrationDTO = new RegistrationDTO();
        validRegistrationDTO.setEmail("test@example.com");
        validRegistrationDTO.setPassword("password123");
        validRegistrationDTO.setConfirmPassword("password123");
        validRegistrationDTO.setCaptcha("123456");

        validLoginDTO = new LoginDTO();
        validLoginDTO.setEmail("test@example.com");
        validLoginDTO.setPassword("password123");
    }

    // sendCode方法测试
    @Test
    void sendCode_ShouldThrowException_WhenEmailIsNull() {
        GetEmailCaptchaDTO dto = new GetEmailCaptchaDTO();
        dto.setEmail(null);

        assertThrows(BusinessException.class, () -> authService.sendCode(dto));
    }

    @Test
    void sendCode_ShouldThrowException_WhenEmailIsInvalid() {
        GetEmailCaptchaDTO dto = new GetEmailCaptchaDTO();
        dto.setEmail("invalid-email");

        assertThrows(BusinessException.class, () -> authService.sendCode(dto));
    }

    @Test
    void sendCode_ShouldThrowException_WhenInCooldown() {
        when(cacheService.exists("captcha:cooldown:test@example.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.sendCode(validCaptchaDTO));
    }

    @Test
    void sendCode_ShouldThrowException_WhenEmailAlreadyRegistered() {
        when(cacheService.exists(anyString())).thenReturn(false);
        when(userMapper.existByUserEmail("test@example.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> authService.sendCode(validCaptchaDTO));
    }

    @Test
    void sendCode_ShouldSendMail_WhenAllConditionsMet() {
        when(cacheService.exists(anyString())).thenReturn(false);
        when(userMapper.existByUserEmail("test@example.com")).thenReturn(false);

        authService.sendCode(validCaptchaDTO);

        verify(cacheService, times(2)).set(anyString(), any(), anyInt());
        verify(mailService).sendSimpleMail(eq("test@example.com"), anyString(), anyString());
    }

    // register方法测试
    @Test
    void register_ShouldThrowException_WhenRegistrationDTOIsInvalid() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setEmail(null);

        assertThrows(BusinessException.class, () -> authService.register(dto));
    }

    @Test
    void register_ShouldThrowException_WhenPasswordsNotMatch() {
        validRegistrationDTO.setConfirmPassword("different");

        assertThrows(BusinessException.class, () -> authService.register(validRegistrationDTO));
    }

    @Test
    void register_ShouldThrowException_WhenCaptchaIsWrong() {
        when(cacheService.get("captcha:valid:test@example.com", String.class)).thenReturn("654321");

        assertThrows(BusinessException.class, () -> authService.register(validRegistrationDTO));
    }

    @Test
    void register_ShouldCreateUserAndRole_WhenAllConditionsMet() {
        // 模拟验证码验证通过
        when(cacheService.get("captcha:valid:test@example.com", String.class)).thenReturn("123456");

        // 模拟用户创建 - 设置ID并返回受影响行数
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // 模拟设置ID
            return 1; // 返回受影响行数
        }).when(userService).createUser(any(User.class));

        // 执行测试
        String result = authService.register(validRegistrationDTO);

        // 验证结果
        assertEquals("注册成功", result);

        // 验证用户创建
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(userCaptor.capture());
        User createdUser = userCaptor.getValue();
        assertEquals("test@example.com", createdUser.getEmail());
        assertTrue(PasswordUtil.verify("password123", createdUser.getPassword()));

        // 验证角色创建
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleService).createUserRole(roleCaptor.capture());
        UserRole createdRole = roleCaptor.getValue();
        assertEquals(1L, createdRole.getUserId());
        assertEquals(RoleConstant.USER_ROLE_ID, createdRole.getRoleId());

        // 验证缓存清理
        verify(cacheService).delete("captcha:valid:test@example.com");
    }


    // login方法测试
    @Test
    void login_ShouldThrowException_WhenCredentialsAreNull() {
        LoginDTO dto = new LoginDTO();
        dto.setEmail(null);
        dto.setPassword(null);

        assertThrows(BusinessException.class, () -> authService.login(dto, request));
    }

    @Test
    void login_ShouldThrowException_WhenEmailFormatIsInvalid() {
        validLoginDTO.setEmail("invalid-email");

        assertThrows(BusinessException.class, () -> authService.login(validLoginDTO, request));
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userMapper.getUserByEmail("test@example.com")).thenReturn(null);
        when(request.getHeader(anyString())).thenReturn("device123");

        assertThrows(BusinessException.class, () -> authService.login(validLoginDTO, request));
        verify(loginRateLimiterService).tryAcquire("device123", "test@example.com");
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsWrong() {
        User mockUser = new User();
        mockUser.setPassword(PasswordUtil.encrypt("wrongpassword"));

        when(userMapper.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(request.getHeader(anyString())).thenReturn("device123");

        assertThrows(BusinessException.class, () -> authService.login(validLoginDTO, request));
        verify(loginRateLimiterService).tryAcquire("device123", "test@example.com");
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreCorrect() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setPassword(PasswordUtil.encrypt("password123"));

        when(userMapper.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(request.getHeader(anyString())).thenReturn("device123");

        String secret = "loveandpeaceQiuWillForeverGoForItAND[9049thghiwehgu2phugbug[bgaw4u9gbbaqgw4u";
        long expiration = 14400000L;
        JwtUtil.init(secret, expiration);

        LoginResponse response = authService.login(validLoginDTO, request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        verify(loginRateLimiterService).reset("device123", "test@example.com");
    }
}