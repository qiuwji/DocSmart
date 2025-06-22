package com.qiu.backend.modules.auth.controller;

import com.qiu.backend.common.core.Result.Result;
import com.qiu.backend.modules.auth.service.AuthService;
import com.qiu.backend.modules.model.dto.GetEmailCaptchaDTO;
import com.qiu.backend.modules.model.dto.LoginDTO;
import com.qiu.backend.modules.model.dto.RegistrationDTO;
import com.qiu.backend.modules.model.result_data.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public Result sendCode(@RequestBody GetEmailCaptchaDTO captchaDTO) {
        authService.sendCode(captchaDTO);
        return Result.success();
    }

    @PostMapping("/register")
    public String register(@RequestBody RegistrationDTO registrationDTO) {
        return authService.register(registrationDTO);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        return authService.login(loginDTO, request);
    }
}
