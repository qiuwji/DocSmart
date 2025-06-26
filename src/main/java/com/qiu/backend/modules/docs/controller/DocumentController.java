package com.qiu.backend.modules.docs.controller;

import com.qiu.backend.common.utils.UserContextHolder;
import com.qiu.backend.modules.docs.service.DocumentService;
import com.qiu.backend.modules.model.dto.FileUploadDTO;
import com.qiu.backend.modules.model.vo.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(("/api/docs"))
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public FileUploadResponse uploadFile(@ModelAttribute FileUploadDTO fileUploadDTO) {
        log.info("用户{}开始上传文件", UserContextHolder.getUserId());

        return documentService.uploadFile(fileUploadDTO);
    }
}
