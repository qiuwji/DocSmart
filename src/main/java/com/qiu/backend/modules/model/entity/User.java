package com.qiu.backend.modules.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private Long id;

    private String username;

    private String email;

    private String password;

    private String avatar;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
