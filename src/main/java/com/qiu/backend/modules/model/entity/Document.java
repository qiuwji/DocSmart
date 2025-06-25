package com.qiu.backend.modules.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Document {

    private Long id;

    private Long userId;

    private Long folderId;

    private String name;

    private String type;

    private Long size;

    private String storagePath;

    private DocumentStatus status;

    private boolean deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
