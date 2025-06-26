package com.qiu.backend.modules.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class FileUploadDTO {

    private Long folderId;

    private List<String> tags;

    private MultipartFile file;
}
