package com.qiu.backend.modules.model.vo;

import lombok.Data;

@Data
public class DownloadDocumentResponse {
    private String url;
    private FileMeta fileMeta;

    @Data
    public static class FileMeta {
        private String name;
        private Long size;
        private String contentType;
    }
}