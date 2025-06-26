package com.qiu.backend.modules.docs.service.validator;

import com.qiu.backend.modules.docs.service.validator.strategy.DocValidator;
import com.qiu.backend.modules.docs.service.validator.strategy.DocxValidator;
import com.qiu.backend.modules.docs.service.validator.strategy.PdfValidator;
import com.qiu.backend.modules.docs.service.validator.strategy.TxtValidator;

public class ValidatorFactory {
    public static IFileTypeValidator getValidator(String type) {
        return switch (type.toLowerCase()) {
            case ".doc" -> new DocValidator();
            case ".docx" -> new DocxValidator();
            case ".pdf" -> new PdfValidator();
            case ".txt" -> new TxtValidator();
            default -> throw new IllegalArgumentException("Unsupported file type: " + type);
        };
    }
}
