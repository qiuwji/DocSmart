package com.qiu.backend.modules.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    Long userId;

    Long roleId;

    // 创建时间
    LocalDateTime createTime;
}
