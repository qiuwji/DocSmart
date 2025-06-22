package com.qiu.backend.modules.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRole {

    Long userId;

    Long roleId;

    // 创建时间
    LocalDateTime createTime;
}
