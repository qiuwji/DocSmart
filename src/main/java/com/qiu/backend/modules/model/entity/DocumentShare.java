package com.qiu.backend.modules.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentShare {

    private Long id;

    private Long documentId;

    private Long userId;

    private String shareCode;

    private String shareLink;

    private LocalDateTime expiredAt;

    private LocalDateTime createdAt;

    private boolean enabled;
}
