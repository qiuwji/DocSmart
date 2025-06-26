package com.qiu.backend.modules.docs.service.validator.strategy;

import com.qiu.backend.modules.docs.service.validator.IFileTypeValidator;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PdfValidator implements IFileTypeValidator {

    @Override
    public boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 检查Content-Type是否为PDF
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            return false;
        }

        // 检查文件魔数(Magic Number)
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[5]; // PDF文件魔数为 "%PDF-"，占5个字节
            int bytesRead = inputStream.read(header);

            if (bytesRead < 5) {
                return false;
            }

            // 转换前5个字节为字符串并比较
            String magicNumber = new String(header, StandardCharsets.US_ASCII);
            return magicNumber.equals("%PDF-");

        } catch (IOException e) {
            // 读取文件失败时返回false
            return false;
        }
    }
}
