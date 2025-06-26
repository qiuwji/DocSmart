package com.qiu.backend.modules.docs.service.validator.strategy;

import com.qiu.backend.modules.docs.service.validator.IFileTypeValidator;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TxtValidator implements IFileTypeValidator {

    @Override
    public boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 1. 检查扩展名
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".txt")) {
            return false;
        }

        // 2. 尝试读取前几行，确保是 UTF-8 文本，不是二进制伪装
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            int linesChecked = 0;
            while (reader.readLine() != null && linesChecked++ < 5) {
                // 只检查能正常读取前几行
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
