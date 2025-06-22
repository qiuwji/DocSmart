package com.qiu.backend.modules.model.dto;

import lombok.Data;

@Data
public class GetEmailCaptchaDTO {

    private String email;

    private String scene;
}
