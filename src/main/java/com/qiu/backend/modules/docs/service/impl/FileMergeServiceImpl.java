package com.qiu.backend.modules.docs.service.impl;

import com.qiu.backend.common.aop.annotation.DistributedLock;
import com.qiu.backend.common.core.constant.FileConstant;
import com.qiu.backend.common.infra.cache.CacheService;
import com.qiu.backend.common.infra.storage.StorageBucket;
import com.qiu.backend.common.infra.storage.StorageService;
import com.qiu.backend.common.utils.FileUtil;
import com.qiu.backend.common.utils.RandomUtil;
import com.qiu.backend.modules.docs.service.FileMergeService;
import com.qiu.backend.modules.model.dto.FileChunkInfo;
import com.qiu.backend.modules.model.dto.FileChunkUploadInitDTO;
import com.qiu.backend.modules.model.entity.Document;
import com.qiu.backend.modules.model.entity.DocumentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDateTime;

@Slf4j
@Service
public class FileMergeServiceImpl implements FileMergeService {

    private final CacheService cacheService;
    private final StorageService storageService;
    private final DocumentServiceImpl documentService;

    @Autowired
    public FileMergeServiceImpl(CacheService cacheService,
                                StorageService storageService,
                                DocumentServiceImpl documentService) {
        this.cacheService = cacheService;
        this.storageService = storageService;
        this.documentService = documentService;
    }

    @Override
    @Transactional
    @DistributedLock(key = "lock:merge")
    public void merge(String fileId, Long userId) throws IOException {
        // Redis key 前缀
        String metaKey = "chunk:meta:" + fileId + ":" + userId;
        String initKey = "chunk:init:" + fileId + ":" + userId;

        // 读取分片元信息
        FileChunkInfo fileChunkInfo = cacheService.get(metaKey, FileChunkInfo.class);
        String originalFileName = fileChunkInfo.getFileName();
        String fileTypeWithDot = FileUtil.getFileExtension(originalFileName);
        // 规范化后缀，不带点
        String extension = fileTypeWithDot.startsWith(".")
                ? fileTypeWithDot.substring(1)
                : fileTypeWithDot;

        // 生成存储文件名和路径
        String secretedFileName = RandomUtil.randomUUID() + "." + extension;
        String storagePath = extension + "/" + secretedFileName;

        // 临时合并文件
        File mergedFile = File.createTempFile("merge-", "-" + originalFileName);

        // 标记“合并中”状态，供定时任务检查或补偿
        String inProgressKey = FileConstant.UPLOAD_IN_PROGRESS_PREFIX + secretedFileName;
        cacheService.set(inProgressKey, storagePath);

        try (OutputStream os = new FileOutputStream(mergedFile)) {
            long totalChunks = fileChunkInfo.getTotalChunks();
            String bucket = StorageBucket.FILES.getBucketName();
            String tempPrefix = "temp/" + fileId + "/" + userId + "/";

            // 按顺序合并所有分片
            for (int i = 0; i < totalChunks; i++) {
                String partName   = "chunk_" + i + ".part";
                String objectName = tempPrefix + partName;
                try (InputStream is = storageService.getObject(bucket, objectName)) {
                    byte[] buffer = new byte[8 * 1024];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                } catch (Exception e) {
                    log.error("读取分片失败: {}", objectName, e);
                    throw new RuntimeException("读取分片失败: " + objectName, e);
                }
            }

            // 上传合并后文件到存储
            try (InputStream mergedInput = new FileInputStream(mergedFile)) {
                storageService.uploadFile(
                        StorageBucket.FILES.getBucketName(),
                        mergedInput,
                        secretedFileName,
                        mergedFile.length()
                );
            } catch (Exception e) {
                log.error("上传合并文件失败", e);
                throw new RuntimeException("上传合并文件失败", e);
            }

            // 写库并关联标签
            FileChunkUploadInitDTO initDTO = cacheService.get(initKey, FileChunkUploadInitDTO.class);
            Document document = buildDocument(
                    originalFileName, secretedFileName, extension,
                    mergedFile.length(), storagePath, userId, initDTO.getFolderId()
            );
            documentService.insertDocument(document);
            documentService.associateTagsWithDocument(document.getId(), initDTO.getTags());

        } finally {
            // 始终删除本地临时合并文件
            if (mergedFile.exists() && !mergedFile.delete()) {
                log.warn("删除本地临时合并文件失败: {}", mergedFile.getAbsolutePath());
            }
        }
    }

    private Document buildDocument(String originalFilename,
                                   String secretedFilename,
                                   String type,
                                   Long size,
                                   String storagePath,
                                   Long userId,
                                   Long folderId) {
        Document doc = new Document();
        doc.setOriginalFilename(originalFilename);
        doc.setName(secretedFilename);
        doc.setType(type);
        doc.setSize(size);
        doc.setStoragePath(storagePath);
        doc.setStatus(DocumentStatus.UPLOADED);
        doc.setUserId(userId);
        doc.setFolderId(folderId);
        doc.setDeleted(false);
        doc.setCreateTime(LocalDateTime.now());
        doc.setUpdateTime(LocalDateTime.now());
        return doc;
    }
}
