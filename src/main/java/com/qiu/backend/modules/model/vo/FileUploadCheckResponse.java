package com.qiu.backend.modules.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class FileUploadCheckResponse {

    private List<Long> uploadChunks;

    private Long totalChunks;

    private boolean isComplete;
}
