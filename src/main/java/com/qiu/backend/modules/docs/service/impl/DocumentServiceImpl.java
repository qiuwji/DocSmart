package com.qiu.backend.modules.docs.service.impl;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.constant.FileConstant;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.common.infra.cache.CacheService;
import com.qiu.backend.common.infra.storage.StorageBucket;
import com.qiu.backend.common.infra.storage.StorageService;
import com.qiu.backend.common.utils.FileUtil;
import com.qiu.backend.common.utils.RandomUtil;
import com.qiu.backend.common.utils.UserContextHolder;
import com.qiu.backend.modules.docs.mapper.DocumentMapper;
import com.qiu.backend.modules.docs.service.DocumentService;
import com.qiu.backend.modules.docs.service.DocumentTagService;
import com.qiu.backend.modules.docs.service.TagService;
import com.qiu.backend.modules.docs.service.validator.IFileTypeValidator;
import com.qiu.backend.modules.docs.service.validator.ValidatorFactory;
import com.qiu.backend.modules.model.dto.FileUploadDTO;
import com.qiu.backend.modules.model.entity.Document;
import com.qiu.backend.modules.model.entity.DocumentStatus;
import com.qiu.backend.modules.model.vo.FileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private final StorageService storageService;
    private final CacheService cacheService;
    private final DocumentMapper documentMapper;
    private final TagService tagService;
    private final DocumentTagService documentTagService;

    @Autowired
    public DocumentServiceImpl(StorageService storageService, CacheService cacheService,
                               DocumentMapper documentMapper, TagService tagService,
                               DocumentTagService documentTagService) {
        this.storageService = storageService;
        this.cacheService = cacheService;
        this.documentMapper = documentMapper;
        this.tagService = tagService;
        this.documentTagService = documentTagService;
    }

    @Override
    @Transactional
    public FileUploadResponse uploadFile(FileUploadDTO fileUploadDTO) {
        validateUploadRequest(fileUploadDTO);

        Long userId = UserContextHolder.getUserId();
        MultipartFile file = fileUploadDTO.getFile();
        String originalFilename = file.getOriginalFilename();

        String extension = FileUtil.getExtension(originalFilename);
        String newFileName = generateNewFileName(extension);
        String storagePath = buildStoragePath(extension, newFileName);

        uploadFileToStorage(file, storagePath);
        markUploadInProgress(newFileName, extension);

        Document document = createAndSaveDocument(userId, fileUploadDTO, file, newFileName, storagePath);
        associateTagsWithDocument(document.getId(), fileUploadDTO.getTags());
        clearUploadProgressMarker(newFileName);

        return buildUploadResponse(document.getId(), storagePath);
    }

    private void validateUploadRequest(FileUploadDTO fileUploadDTO) {
        if (fileUploadDTO == null || fileUploadDTO.getFile() == null) {
            throw new IllegalArgumentException("文件上传参数不能为空");
        }

        validateFolderId(fileUploadDTO.getFolderId());
        validateTags(fileUploadDTO.getTags());
        validateFileSize(fileUploadDTO.getFile());
        validateFileType(fileUploadDTO.getFile());
    }

    private void validateFolderId(Long folderId) {
        if (folderId == null) {
            throw new IllegalArgumentException("目录不能为空");
        }
        // 可以添加进一步的文件夹存在性验证
    }

    private void validateTags(List<String> tags) {
        if (tags != null && tags.size() > FileConstant.MAX_TAGS_COUNT) {
            throw new IllegalArgumentException("标签数不能大于" + FileConstant.MAX_TAGS_COUNT);
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (!FileUtil.isSizeValid(file, FileConstant.FILE_MAX_MB)) {
            throw new IllegalArgumentException("文件大小不能超过：" + FileConstant.FILE_MAX_MB + "MB");
        }
    }

    private void validateFileType(MultipartFile file) {
        String extension = FileUtil.getExtension(file.getOriginalFilename());
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("文件缺少扩展名");
        }

        IFileTypeValidator validator = ValidatorFactory.getValidator(extension);
        if (!validator.isValid(file)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }
    }

    private String generateNewFileName(String extension) {
        return RandomUtil.randomUUID() + extension;
    }

    private String buildStoragePath(String extension, String fileName) {
        if (extension == null || extension.length() < 2) {
            throw new IllegalArgumentException("无效的文件扩展名");
        }
        return extension.substring(1) + "/" + fileName;
    }

    private void markUploadInProgress(String fileName, String extension) {
        String key = FileConstant.UPLOAD_IN_PROGRESS_PREFIX + fileName;
        // 存储的是文件路径
        cacheService.set(key, extension.substring(1) + "/" + fileName);
    }

    private void clearUploadProgressMarker(String fileName) {
        String key = FileConstant.UPLOAD_IN_PROGRESS_PREFIX + fileName;
        cacheService.delete(key);
    }

    private void uploadFileToStorage(MultipartFile file, String storagePath) {
        try {
            storageService.uploadFile(
                    StorageBucket.FILES.getBucketName(),
                    file.getInputStream(),
                    storagePath,
                    file.getSize()
            );
            log.info("文件成功上传到存储: {}", storagePath);
        } catch (IOException e) {
            log.error("文件流读取失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FAILED, "文件读取失败");
        } catch (Exception e) {
            log.error("文件上传到存储失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.FAILED, "文件上传失败: " + e.getMessage());
        }
    }

    private Document createAndSaveDocument(Long userId, FileUploadDTO fileUploadDTO,
                                           MultipartFile file, String fileName, String storagePath) {
        Document document = new Document();
        document.setSize(file.getSize());
        document.setDeleted(false);
        document.setUserId(userId);
        document.setFolderId(fileUploadDTO.getFolderId());
        document.setName(fileName);
        document.setType(FileUtil.getExtension(file.getOriginalFilename()));
        document.setStoragePath(storagePath);
        document.setStatus(DocumentStatus.UPLOADED);
        document.setCreateTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());

        documentMapper.insert(document);
        log.info("文档记录创建成功, ID: {}", document.getId());

        return document;
    }

    private void associateTagsWithDocument(Long documentId, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }

        List<Long> tagIds = tagService.getOrCreateTagIdsByName(tags, UserContextHolder.getUserId());
        documentTagService.insertBatch(documentId, tagIds);
        log.info("为文档 {} 关联了 {} 个标签", documentId, tagIds.size());
    }

    private FileUploadResponse buildUploadResponse(Long documentId, String storagePath) {
        FileUploadResponse response = new FileUploadResponse();
        response.setId(documentId);
        response.setStoragePath(storagePath);
        return response;
    }
}