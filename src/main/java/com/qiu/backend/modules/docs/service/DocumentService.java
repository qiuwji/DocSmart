package com.qiu.backend.modules.docs.service;

import com.qiu.backend.modules.model.dto.FileChunkUploadDTO;
import com.qiu.backend.modules.model.dto.FileChunkUploadInitDTO;
import com.qiu.backend.modules.model.dto.FileUploadDTO;
import com.qiu.backend.modules.model.entity.Document;
import com.qiu.backend.modules.model.vo.DownloadDocumentResponse;
import com.qiu.backend.modules.model.vo.FileChunkUploadResponse;
import com.qiu.backend.modules.model.vo.FileUploadCheckResponse;
import com.qiu.backend.modules.model.vo.FileUploadResponse;

public interface DocumentService {

    FileUploadResponse uploadFile(FileUploadDTO fileUploadDTO);

    DownloadDocumentResponse downloadDocument(Long id);

    Document getDocumentInfo(Long id);

    FileChunkUploadResponse uploadChunk(FileChunkUploadDTO fileChunkUploadDTO);

    FileUploadCheckResponse check(String fileId);

    void insertDocument(Document document);

    void initUploadChunk(FileChunkUploadInitDTO fileChunkUploadInitDTO);
}
