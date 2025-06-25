package com.qiu.backend.modules.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentMeta {

    private Long documentId;

    private String author;

    private int pageCount;

    private String parsedText;

    private LocalDateTime createAt;
}
