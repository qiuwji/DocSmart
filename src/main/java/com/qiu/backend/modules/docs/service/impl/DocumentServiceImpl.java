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
import com.qiu.backend.modules.model.dto.FileChunkInfo;
import com.qiu.backend.modules.model.dto.FileChunkUploadDTO;
import com.qiu.backend.modules.model.dto.FileChunkUploadInitDTO;
import com.qiu.backend.modules.model.dto.FileUploadDTO;
import com.qiu.backend.modules.model.entity.Document;
import com.qiu.backend.modules.model.entity.DocumentStatus;
import com.qiu.backend.modules.model.vo.DownloadDocumentResponse;
import com.qiu.backend.modules.model.vo.FileChunkUploadResponse;
import com.qiu.backend.modules.model.vo.FileUploadCheckResponse;
import com.qiu.backend.modules.model.vo.FileUploadResponse;
import com.qiu.backend.modules.mq.message.FileUploadFinishedMessage;
import com.qiu.backend.modules.mq.producer.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private final StorageService storageService;
    private final CacheService cacheService;
    private final DocumentMapper documentMapper;
    private final TagService tagService;
    private final DocumentTagService documentTagService;
    private final MessageProducer messageProducer;

    @Autowired
    public DocumentServiceImpl(StorageService storageService, CacheService cacheService,
                               DocumentMapper documentMapper, TagService tagService,
                               DocumentTagService documentTagService,
                               MessageProducer messageProducer) {
        this.storageService = storageService;
        this.cacheService = cacheService;
        this.documentMapper = documentMapper;
        this.tagService = tagService;
        this.documentTagService = documentTagService;
        this.messageProducer = messageProducer;
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

    @Override
    public DownloadDocumentResponse downloadDocument(Long id) {
        if (id == null) throw new IllegalArgumentException("文件id不能为空");

        Document documentInfo = getDocumentInfo(id);

        String bucket = documentInfo.getType().substring(1);
        DownloadDocumentResponse downloadDocumentResponse = new DownloadDocumentResponse();
        String storagePath = documentInfo.getStoragePath();

        String key = FileConstant.UPLOAD_IN_PROGRESS_PREFIX + id;
        if (!cacheService.exists(key)) {
            try {
                String temporarilyFileUrl = storageService.getTemporarilyFileUrl(bucket, storagePath, 60 * 24);
                downloadDocumentResponse.setUrl(temporarilyFileUrl);
                cacheService.set(key, temporarilyFileUrl, FileConstant.DOCUMENT_URL_TTL, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("文件下载URL生成失败 | docId={} | bucket={} | path={}",
                        id, bucket, storagePath, e);
                throw new BusinessException(ResultCode.FAILED);
            }
        } else {
            downloadDocumentResponse.setUrl(cacheService.get(key, String.class));
        }


        DownloadDocumentResponse.FileMeta fileMeta = new DownloadDocumentResponse.FileMeta();
        fileMeta.setContentType(bucket);
        fileMeta.setName(documentInfo.getName());
        fileMeta.setSize(documentInfo.getSize());

        downloadDocumentResponse.setFileMeta(fileMeta);

        return downloadDocumentResponse;
    }

    @Override
    public Document getDocumentInfo(Long id) {
        return documentMapper.getById(id);
    }

    @Override
    public FileChunkUploadResponse uploadChunk(FileChunkUploadDTO dto) {
        MultipartFile chunk = dto.getChunk();
        if (chunk == null || chunk.isEmpty()) {
            throw new IllegalArgumentException("上传的分片不能为空文件");
        }

        Long userId = UserContextHolder.getUserId();
        String fileId = dto.getFileId();  // 前端传的唯一 fileId

        String chunkListKey = buildChunkListKey(fileId, userId);
        String chunkMetaKey = buildChunkMetaKey(fileId, userId);

        // 获取或初始化文件元信息
        FileChunkInfo info = cacheService.get(chunkMetaKey, FileChunkInfo.class);
        if (info == null) {
            info = new FileChunkInfo();
            info.setTotalChunks(dto.getTotalChunks());
            info.setFileName(dto.getFilename());
            info.setUpdateTime(LocalDateTime.now());
            info.setFileId(fileId);
        }

        // 获取已上传分片列表，防空
        List<String> uploaded = cacheService.getAllFromList(chunkListKey);
        if (uploaded == null) {
            uploaded = new ArrayList<>();
        }
        String currentChunk = String.valueOf(dto.getChunkNumber());

        if (!uploaded.contains(currentChunk)) {
            // 上传分片到 MinIO 临时目录
            String chunkName = "chunk_" + currentChunk + ".part";
            // 路径中加入 userId，避免同名冲突
            String storagePath = "temp/" + fileId + "/" + userId + "/" + chunkName;
            uploadFileToStorage(chunk, storagePath);

            // 记录分片编号到 Redis list
            cacheService.rightPush(chunkListKey, currentChunk);
            // 延长列表过期（2 天）
            cacheService.expire(chunkListKey, 2, TimeUnit.DAYS);
        }

        // 更新元信息最后更新时间（不设置过期，统一由调度清理）
        info.setUpdateTime(LocalDateTime.now());
        cacheService.set(chunkMetaKey, info);

        // 判断是否完成
        long uploadedCount = cacheService.listSize(chunkListKey);
        boolean isComplete = info.getTotalChunks().equals(uploadedCount);

        if (isComplete) {
            // 发送消息给消费者
            FileUploadFinishedMessage message = new FileUploadFinishedMessage(fileId, userId);
            messageProducer.send("file-merge-topic", message);
        }

        return new FileChunkUploadResponse(true, isComplete);
    }

    @Override
    public FileUploadCheckResponse check(String fileId) {
        Long userId = UserContextHolder.getUserId();

        String chunkListKey = buildChunkListKey(fileId, userId);
        String chunkMetaKey = buildChunkMetaKey(fileId, userId);

        // 1. 获取元信息
        FileChunkInfo info = cacheService.get(chunkMetaKey, FileChunkInfo.class);
        if (info == null) {
            throw new IllegalStateException("找不到该文件的上传信息，可能已过期或未开始上传");
        }
        Long totalChunks = info.getTotalChunks();

        // 2. 获取已上传分片列表
        List<String> chunkStrList = cacheService.getAllFromList(chunkListKey);
        List<Long> uploaded = new ArrayList<>();
        if (chunkStrList != null) {
            for (String s : chunkStrList) {
                try {
                    uploaded.add(Long.parseLong(s));
                } catch (NumberFormatException e) {
                    log.warn("解析分片编号失败：{}", s);
                }
            }
        }

        // 3. 判断完成
        boolean isComplete = totalChunks != null && uploaded.size() >= totalChunks;

        return new FileUploadCheckResponse(uploaded, totalChunks, isComplete);
    }

    @Override
    public void insertDocument(Document document) {
        documentMapper.insert(document);
    }

    @Override
    public void initUploadChunk(FileChunkUploadInitDTO fileChunkUploadInitDTO) {
        String key = "chunk:init:" + fileChunkUploadInitDTO.getFileId() + ":" + UserContextHolder.getUserId();
        cacheService.set(key, fileChunkUploadInitDTO);
    }

    /** 构造 Redis 分片列表 Key，自动包含 fileId 和 userId */
    private String buildChunkListKey(String fileId, Long userId) {
        return "chunk:upload:" + fileId + ":" + userId;
    }

    /** 构造 Redis 元信息 Key，自动包含 fileId 和 userId */
    private String buildChunkMetaKey(String fileId, Long userId) {
        return "chunk:meta:" + fileId + ":" + userId;
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
        document.setOriginalFilename(file.getOriginalFilename());
        document.setType(FileUtil.getExtension(file.getOriginalFilename()));
        document.setStoragePath(storagePath);
        document.setStatus(DocumentStatus.UPLOADED);
        document.setCreateTime(LocalDateTime.now());
        document.setUpdateTime(LocalDateTime.now());

        documentMapper.insert(document);
        log.info("文档记录创建成功, ID: {}", document.getId());

        return document;
    }

    public void associateTagsWithDocument(Long documentId, List<String> tags) {
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