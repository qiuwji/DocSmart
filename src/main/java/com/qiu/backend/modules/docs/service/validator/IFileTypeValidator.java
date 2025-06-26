package com.qiu.backend.modules.docs.service.validator;

import org.springframework.web.multipart.MultipartFile;

public interface IFileTypeValidator {

    boolean isValid(MultipartFile file);
}
