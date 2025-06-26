package com.qiu.backend.modules.auth.service.impl;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.constant.CacheConstant;
import com.qiu.backend.common.core.constant.RoleConstant;
import com.qiu.backend.common.core.constant.SceneConstant;
import com.qiu.backend.common.core.constant.UserConstant;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.common.infra.cache.impl.RedisCacheService;
import com.qiu.backend.common.rate.LoginRateLimiterService;
import com.qiu.backend.common.utils.*;
import com.qiu.backend.modules.auth.service.AuthService;
import com.qiu.backend.modules.auth.service.MailService;
import com.qiu.backend.modules.docs.service.FolderService;
import com.qiu.backend.modules.model.dto.GetEmailCaptchaDTO;
import com.qiu.backend.modules.model.dto.LoginDTO;
import com.qiu.backend.modules.model.dto.RegistrationDTO;
import com.qiu.backend.modules.model.entity.Folder;
import com.qiu.backend.modules.model.entity.User;
import com.qiu.backend.modules.model.entity.UserRole;
import com.qiu.backend.modules.model.vo.LoginResponse;
import com.qiu.backend.modules.user.mapper.UserMapper;
import com.qiu.backend.modules.user.service.UserRoleService;
import com.qiu.backend.modules.user.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.qiu.backend.common.core.Result.ResultCode.*;
import static com.qiu.backend.common.core.constant.CacheConstant.CAPTCHA_COOLDOWN_PREFIX;
import static com.qiu.backend.common.core.constant.CacheConstant.CAPTCHA_VALID_PREFIX;
import static com.qiu.backend.common.core.constant.UserConstant.TOKEN_VALID_PREFIX;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final RedisCacheService cacheService;

    private final MailService mailService;

    private final UserService userService;

    private final UserRoleService userRoleService;

    private final UserMapper userMapper;

    private final LoginRateLimiterService loginRateLimiterService;

    private final FolderService folderService;

    @Autowired
    public AuthServiceImpl(RedisCacheService redisCacheService, MailService mailService,
                           UserService userService, UserRoleService userRoleService,
                           UserMapper userMapper,LoginRateLimiterService loginRateLimiterService,
                           FolderService folderService) {
        this.mailService = mailService;
        this.cacheService = redisCacheService;
        this.userService = userService;
        this.userRoleService = userRoleService;
        this.userMapper = userMapper;
        this.loginRateLimiterService = loginRateLimiterService;
        this.folderService = folderService;
    }

    public void sendCode(GetEmailCaptchaDTO captchaDTO) {
        log.info("给邮箱为{}的用户发送验证码", captchaDTO.getEmail());

        if (StringUtils.isEmpty(captchaDTO.getEmail()) || !EmailValidatorUtil.isValid(captchaDTO.getEmail())) {
            throw new BusinessException(VALIDATE_FAILED);
        }

        String cooldownKey = CAPTCHA_COOLDOWN_PREFIX + captchaDTO.getEmail();

        // 判断是否可发验证码
        if (cacheService.exists(cooldownKey)) {
            throw new BusinessException(ResultCode.COOLDOWN_FAILED);
        }

        if (captchaDTO.getScene() != null && captchaDTO.getScene().equals(SceneConstant.REGISTER_CONSTANT)) {
            if (userMapper.existByUserEmail(captchaDTO.getEmail())) {
                throw new BusinessException(REGISTERED, "该邮箱已经注册过账号了");
            }
        }
        // 生成验证码：6位
        String captcha = RandomUtil.randomNumbers(6);
        String captchaKey = CAPTCHA_VALID_PREFIX + captchaDTO.getEmail();
        // 存入缓存
        cacheService.set(captchaKey, captcha, CacheConstant.VALID_TIME);
        cacheService.set(cooldownKey, 1, CacheConstant.COOLDOWN_TIME);

        mailService.sendSimpleMail(captchaDTO.getEmail(), "验证码", "您的验证码是：" + captcha);
    }

    private void argumentValid(@NotNull RegistrationDTO registrationDTO) {
        if (registrationDTO.getEmail() == null || registrationDTO.getPassword() == null ||
                registrationDTO.getConfirmPassword() == null || registrationDTO.getCaptcha() == null
                || registrationDTO.getEmail().isEmpty()) {

            throw new BusinessException(VALIDATE_FAILED);
        }

        if (!EmailValidatorUtil.isValid(registrationDTO.getEmail())) {
            throw new BusinessException(EMAIL_FORMAT_ERROR);
        }

        // 判断密码和验证密码是否相同
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "两次输入的密码不一致");
        }
    }

    private User generateUserFromDTO(RegistrationDTO registrationDTO) {
        User user = new User();

        user.setAvatar(UserConstant.DEFAULT_AVATAR_NAME);
        user.setEmail(registrationDTO.getEmail());

        // 对密码加密存储
        String encryptedPassword = PasswordUtil.encrypt(registrationDTO.getPassword());
        user.setPassword(encryptedPassword);

        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        String username;

        do {
            username = "user_" + RandomUtil.randomLetters(7);
        } while (userService.existByUserName(username));

        user.setUsername(username);

        return user;
    }

    private UserRole generateUserRole(Long id) {
        UserRole userRole = new UserRole();

        userRole.setUserId(id);
        // 默认是普通用户权限
        userRole.setRoleId(RoleConstant.USER_ROLE_ID);

        userRole.setCreateTime(LocalDateTime.now());

        return userRole;
    }

    @Override
    @Transactional
    public String register(RegistrationDTO registrationDTO) {
        log.info("邮箱为{}的用户尝试注册", registrationDTO.getEmail());
        argumentValid(registrationDTO);

        // 查询验证码是否正确
        String captchaKey = CAPTCHA_VALID_PREFIX + registrationDTO.getEmail();
        String realCaptcha = cacheService.get(captchaKey, String.class);

        if (realCaptcha == null || !realCaptcha.equals(registrationDTO.getCaptcha())) {
            throw new BusinessException(FAILED, "验证码错误");
        }

        // 插入用户到用户表
        User user = generateUserFromDTO(registrationDTO);
        userService.createUser(user);

        // 插入权限表
        UserRole userRole = generateUserRole(user.getId());
        userRoleService.createUserRole(userRole);

        cacheService.delete(captchaKey);

        // 创建根目录
        String folderName = "user" + user.getId() + "_root";
        folderService.createFolder(user.getId(), folderName, 0L);

        log.info("用户注册成功: {}", user.getId());
        return "注册成功";
    }

    @Override
    public LoginResponse login(LoginDTO loginDTO, HttpServletRequest request) {
        if (loginDTO.getEmail() == null || loginDTO.getPassword() == null) {
            throw new BusinessException(VALIDATE_FAILED, "账号密码不能为空");
        }

        // 校验邮箱格式是否正确
        if (!EmailValidatorUtil.isValid(loginDTO.getEmail())) {
            throw new BusinessException(EMAIL_FORMAT_ERROR);
        }

        User user = userMapper.getUserByEmail(loginDTO.getEmail());

        String deviceId = DeviceIdUtil.getDeviceId(request);

        // 验证账号或密码是否正确
        if (user == null || !PasswordUtil.verify(loginDTO.getPassword(), user.getPassword())) {
            // 账号密码不正确，进行限流
            if (!loginRateLimiterService.tryAcquire(deviceId, loginDTO.getEmail())) {
                long expire = cacheService.getExpire(loginRateLimiterService.generateKey(deviceId, loginDTO.getEmail()));
                String message = "请" + expire / 60 + "分钟后重试";
                throw new BusinessException(ResultCode.COOLDOWN_FAILED, message);
            }
            throw new BusinessException(ResultCode.EMAIL_OR_PASSWORD_FAILED);
        }

        // 如果登录成功能，删除key
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            loginRateLimiterService.reset(deviceId, loginDTO.getEmail());
            String redisKey = TOKEN_VALID_PREFIX + user.getId();
            cacheService.set(redisKey, user.getId(), 4 * 60 * 60);
            return new LoginResponse(JwtUtil.generateToken(redisKey));
        } finally {
            lock.unlock();
        }
    }
}
