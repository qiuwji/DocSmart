package com.qiu.backend.common.utils;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件操作工具类
 */
public class FileUtil {

    /**
     * 获取文件扩展名（包含点）如 ".jpg"
     * @param filename 原始文件名
     * @return 扩展名（如 ".jpg"），若没有扩展名返回空串
     */
    public static String getExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') < 0) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }

    // 允许的图片MIME类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    // 允许的文件扩展名
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".webp"
    );

    /**
     * 校验文件是否为允许的图片类型（通过MIME类型和文件头）
     * @param file 上传的文件
     * @return true=校验通过
     */
    public static boolean isImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 1. 校验MIME类型
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return false;
        }

        // 2. 校验文件头（防止伪造扩展名）
        try {
            byte[] fileHeader = new byte[12];
            file.getInputStream().read(fileHeader);
            return isImageHeader(fileHeader);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 校验文件大小是否在限制范围内
     * @param file 文件
     * @param maxSizeInMB 最大大小（MB）
     */
    public static boolean isSizeValid(MultipartFile file, int maxSizeInMB) {
        if (file == null) return false;
        return file.getSize() <= maxSizeInMB * 1024 * 1024L;
    }

    /**
     * 生成安全的文件名（防止路径遍历攻击）
     * @param originalFilename 原始文件名
     * @return 安全文件名（UUID + 扩展名）
     */
    public static String generateSafeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return UUID.randomUUID().toString();
        }

        // 提取扩展名
        String extension = getFileExtension(originalFilename);

        // 生成UUID文件名
        return UUID.randomUUID() + (extension.isEmpty() ? "" : extension);
    }

    /**
     * 获取文件扩展名（包含点）
     * @param filename 文件名
     * @return 如 ".jpg"
     */
    public static String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf(".");
        return (dotIndex == -1) ? "" : filename.substring(dotIndex).toLowerCase();
    }

    /**
     * 删除文件（静默失败）
     * @param filePath 文件完整路径
     */
    public static void deleteSilently(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 静默处理删除失败
        }
    }

    // -------------------------- 私有方法 --------------------------

    /**
     * 通过文件头判断是否为图片
     */
    private static boolean isImageHeader(byte[] header) {
        // JPEG: FF D8 FF
        if (header.length >= 3 &&
                header[0] == (byte) 0xFF &&
                header[1] == (byte) 0xD8 &&
                header[2] == (byte) 0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if (header.length >= 8 &&
                header[0] == (byte) 0x89 &&
                header[1] == (byte) 0x50 &&
                header[2] == (byte) 0x4E &&
                header[3] == (byte) 0x47) {
            return true;
        }

        // WEBP: RIFF....WEBP
        if (header.length >= 12 &&
                header[0] == 'R' && header[1] == 'I' &&
                header[2] == 'F' && header[3] == 'F' &&
                header[8] == 'W' && header[9] == 'E' &&
                header[10] == 'B' && header[11] == 'P') {
            return true;
        }

        return false;
    }
}