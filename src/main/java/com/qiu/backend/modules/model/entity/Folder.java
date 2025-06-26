package com.qiu.backend.modules.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Folder {

    private Long id;

    private Long userId;

    private String name;

    private Long parentId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
