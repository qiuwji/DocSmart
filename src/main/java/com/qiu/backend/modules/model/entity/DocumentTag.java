package com.qiu.backend.modules.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentTag {

    private Long documentId;

    private Long tagId;

    private LocalDateTime createTime;
}
