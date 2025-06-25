package com.qiu.backend.modules.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserNameChangeDTO {

    @NotBlank(message = "新用户名不能为空")
    private String username;
}
