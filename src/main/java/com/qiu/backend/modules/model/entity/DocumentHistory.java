package com.qiu.backend.modules.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentHistory {

    private Long id;

    private Long userId;

    private Long documentId;

    private String action;

    private LocalDateTime actionTime;
}
