package com.qiu.backend.modules.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    private Long id;

    private String name;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
