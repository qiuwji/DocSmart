package com.qiu.backend.modules.docs.service;

import com.qiu.backend.modules.model.dto.FileUploadDTO;
import com.qiu.backend.modules.model.vo.FileUploadResponse;

public interface DocumentService {


    FileUploadResponse uploadFile(FileUploadDTO fileUploadDTO);
}
