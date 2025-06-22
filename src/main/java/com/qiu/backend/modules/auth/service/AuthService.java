package com.qiu.backend.modules.auth.service;

import com.qiu.backend.modules.model.dto.GetEmailCaptchaDTO;
import com.qiu.backend.modules.model.dto.LoginDTO;
import com.qiu.backend.modules.model.dto.RegistrationDTO;
import com.qiu.backend.modules.model.result_data.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    void sendCode(GetEmailCaptchaDTO captchaDTO);

    String register(RegistrationDTO registrationDTO);

    LoginResponse login(LoginDTO loginDTO, HttpServletRequest request);
}
