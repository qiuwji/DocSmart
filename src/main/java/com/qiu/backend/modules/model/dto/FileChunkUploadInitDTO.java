package com.qiu.backend.modules.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class FileChunkUploadInitDTO {

    String fileId;

    Long folderId;

    List<String> tags;
}
