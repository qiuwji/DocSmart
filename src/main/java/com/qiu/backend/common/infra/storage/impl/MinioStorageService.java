package com.qiu.backend.common.infra.storage.impl;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.common.infra.config.MinioProperties;
import com.qiu.backend.common.infra.storage.StorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnProperty(name = "app.storage.type", havingValue = "minio")
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;

    @Autowired
    public MinioStorageService(MinioClient minioClient, MinioProperties minioProperties) {
        log.info("Minio服务正在初始化");
        this.minioClient = minioClient;
        log.info("Minio服务初始化完成");
    }

    @Override
    public String uploadFile(String bucket, InputStream inputStream, String fileName, Long fileSize) throws Exception {
        if (!StringUtils.hasText(bucket) || !minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            throw new BusinessException(ResultCode.FAILED, "不存在该桶");
        }

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(fileName)
                        .stream(inputStream, fileSize, -1)
                        .build()
        );

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(fileName)
                        .expiry(7, TimeUnit.DAYS)
                        .build()
        );
    }

    @Override
    public String getFileUrl(String bucketName, String fileName) throws Exception {
        // 参数校验
        if (!StringUtils.hasText(bucketName) || !StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("Bucket名称和文件名不能为空");
        }

        // 检查桶是否存在
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            throw new BusinessException(ResultCode.FAILED, "指定的存储桶不存在");
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7, TimeUnit.DAYS)
                            .build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAILED, "获取文件URL失败：" + e.getMessage());
        }
    }

    @Override
    public String getTemporarilyFileUrl(String bucketName, String fileName, int expiry) throws Exception {
        // 参数校验
        if (!StringUtils.hasText(bucketName) || !StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("Bucket名称和文件名不能为空");
        }
        if (expiry <= 0) {
            throw new IllegalArgumentException("过期时间必须大于0");
        }

        // 检查桶是否存在
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            throw new BusinessException(ResultCode.FAILED, "指定的存储桶不存在");
        }

        try {
            // 获取预签名URL（临时访问）
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(expiry, TimeUnit.MINUTES) // 使用传入的过期时间
                            .build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAILED, "获取临时文件URL失败：" + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String bucketName, String fileName) throws Exception {
        if (!StringUtils.hasText(bucketName) || !StringUtils.hasText(fileName)) {
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error("删除文件失败: bucket={}, file={}", bucketName, fileName, e);
            throw new BusinessException(ResultCode.FAILED, "文件删除失败");
        }

        // 后续处理索引删除的问题
    }

    @Override
    public String moveFile(String sourceBucket, String sourceKey,
                           String targetBucket, String targetKey,
                           boolean overwrite) throws Exception {
        // 参数校验
        if (!StringUtils.hasText(sourceBucket) || !StringUtils.hasText(sourceKey) ||
                !StringUtils.hasText(targetBucket) || !StringUtils.hasText(targetKey)) {
            throw new IllegalArgumentException("桶名和文件键不能为空");
        }

        // 检查源桶是否存在
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(sourceBucket).build())) {
            throw new BusinessException(ResultCode.FAILED, "源存储桶不存在: " + sourceBucket);
        }

        // 检查目标桶是否存在
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(targetBucket).build())) {
            throw new BusinessException(ResultCode.FAILED, "目标存储桶不存在: " + targetBucket);
        }

        // 检查源文件是否存在
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(sourceBucket)
                    .object(sourceKey)
                    .build());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAILED, "源文件不存在: " + sourceKey);
        }

        // 检查目标文件是否已存在
        boolean targetExists = false;
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(targetBucket)
                    .object(targetKey)
                    .build());
            targetExists = true;
        } catch (Exception ignored) {
            // 目标文件不存在是正常情况
        }

        if (targetExists && !overwrite) {
            throw new BusinessException(ResultCode.FAILED, "目标文件已存在且不允许覆盖: " + targetKey);
        }

        try {
            // Minio没有直接的move操作，需要先复制再删除
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder()
                                    .bucket(sourceBucket)
                                    .object(sourceKey)
                                    .build())
                            .bucket(targetBucket)
                            .object(targetKey)
                            .build());

            // 复制成功后删除源文件
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(sourceBucket)
                            .object(sourceKey)
                            .build());

            // 返回新文件的访问URL
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(targetBucket)
                            .object(targetKey)
                            .expiry(7, TimeUnit.DAYS)
                            .build());
        } catch (Exception e) {
            log.error("移动文件失败: sourceBucket={}, sourceKey={}, targetBucket={}, targetKey={}",
                    sourceBucket, sourceKey, targetBucket, targetKey, e);
            throw new BusinessException(ResultCode.FAILED, "文件移动失败: " + e.getMessage());
        }
    }
}
