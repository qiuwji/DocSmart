package com.qiu.backend.modules.docs.service.validator.strategy;

import com.qiu.backend.modules.docs.service.validator.IFileTypeValidator;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class DocValidator implements IFileTypeValidator {

    private static final byte[] DOC_MAGIC = new byte[]{
            (byte) 0xD0, (byte) 0xCF, (byte) 0x11, (byte) 0xE0,
            (byte) 0xA1, (byte) 0xB1, (byte) 0x1A, (byte) 0xE1
    };

    @Override
    public boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 1. 检查扩展名
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".doc")) {
            return false;
        }

        // 2. 检查魔数
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int bytesRead = is.read(header);
            if (bytesRead < 8) {
                return false;
            }

            for (int i = 0; i < DOC_MAGIC.length; i++) {
                if (header[i] != DOC_MAGIC[i]) {
                    return false;
                }
            }

            return true; // 扩展名 + 魔数均匹配
        } catch (IOException e) {
            return false;
        }
    }
}
