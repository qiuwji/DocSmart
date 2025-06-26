package com.qiu.backend.modules.docs.service.validator.strategy;

import com.qiu.backend.modules.docs.service.validator.IFileTypeValidator;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DocxValidator implements IFileTypeValidator {

    private static final byte[] ZIP_MAGIC = new byte[]{
            (byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04
    };

    @Override
    public boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".docx")) {
            return false;
        }

        try (InputStream is = file.getInputStream()) {
            // 检查魔数
            byte[] header = new byte[4];
            if (is.read(header) < 4) {
                return false;
            }
            for (int i = 0; i < ZIP_MAGIC.length; i++) {
                if (header[i] != ZIP_MAGIC[i]) {
                    return false;
                }
            }

            // 检查 ZIP 包内部结构是否包含 /word/document.xml
            try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if ("word/document.xml".equals(entry.getName())) {
                        return true;
                    }
                }
            }

        } catch (IOException e) {
            return false;
        }

        return false;
    }
}
