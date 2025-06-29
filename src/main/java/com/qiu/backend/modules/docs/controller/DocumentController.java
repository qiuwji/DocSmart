package com.qiu.backend.modules.docs.controller;

import com.qiu.backend.common.core.Result.Result;
import com.qiu.backend.common.utils.UserContextHolder;
import com.qiu.backend.modules.docs.service.DocumentService;
import com.qiu.backend.modules.model.dto.FileChunkUploadDTO;
import com.qiu.backend.modules.model.dto.FileChunkUploadInitDTO;
import com.qiu.backend.modules.model.dto.FileUploadDTO;
import com.qiu.backend.modules.model.vo.DownloadDocumentResponse;
import com.qiu.backend.modules.model.vo.FileChunkUploadResponse;
import com.qiu.backend.modules.model.vo.FileUploadCheckResponse;
import com.qiu.backend.modules.model.vo.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.simpleframework.xml.core.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/download/{id}")
    public DownloadDocumentResponse downloadDocument(@PathVariable Long id) {
        log.info("用户id为{}的用户开始下载文档id为{}的文档", UserContextHolder.getUserId(), id);

        return documentService.downloadDocument(id);
    }

    @PostMapping("/upload/chunk")
    public FileChunkUploadResponse uploadChunk(@Validate @ModelAttribute FileChunkUploadDTO fileChunkUploadDTO) {
        log.info("接收到上传分片请求: fileId={}, fileName={}, userId={}, chunkNumber={}/{}",
                fileChunkUploadDTO.getFileId(), fileChunkUploadDTO.getFilename(), UserContextHolder.getUserId(),
                fileChunkUploadDTO.getChunkNumber(), fileChunkUploadDTO.getTotalChunks());

        return documentService.uploadChunk(fileChunkUploadDTO);
    }

    @GetMapping("/upload/check/{fileId}")
    public FileUploadCheckResponse check(@PathVariable String fileId) {
        return documentService.check(fileId);
    }

    @PostMapping("/upload/chunk/init")
    public Result<Void> initUploadChunk(@RequestBody FileChunkUploadInitDTO fileChunkUploadInitDTO) {
        documentService.initUploadChunk(fileChunkUploadInitDTO);

        return Result.success();
    }
}
