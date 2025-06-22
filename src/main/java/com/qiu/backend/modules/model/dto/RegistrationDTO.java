package com.qiu.backend.modules.model.dto;

import lombok.Data;

@Data
public class RegistrationDTO {

    private String email;

    private String password;

    private String confirmPassword;

    private String captcha;
}
