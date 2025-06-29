package com.qiu.backend.modules.docs.schedule;

import com.qiu.backend.common.infra.cache.CacheService;
import com.qiu.backend.common.infra.storage.StorageBucket;
import com.qiu.backend.common.infra.storage.StorageService;
import com.qiu.backend.modules.model.dto.FileChunkInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TTLFileChecker {

    private final StorageService storageService;
    private final CacheService cacheService;

    public TTLFileChecker(StorageService storageService,
                          CacheService cacheService) {
        this.storageService = storageService;
        this.cacheService = cacheService;
    }

    // 每两小时检查一次（7200000 毫秒）
    @Scheduled(fixedDelay = 7200000)
    public void checkOrphanFiles() {
        log.info("开始执行超时未继续上传文件检查");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(2);

        // 1. 遍历所有 chunk:meta:* 的 key
        Set<String> metaKeys = cacheService.keys("chunk:meta:*");
        for (String metaKey : metaKeys) {
            FileChunkInfo info = cacheService.get(metaKey, FileChunkInfo.class);
            if (info == null || info.getUpdateTime().isAfter(cutoff)) {
                continue;  // 未过期或正在活跃上传
            }

            try {
                // 2. 从 metaKey 解析 realFileId 和 userId
                // metaKey 格式： chunk:meta:{realFileId}:{userId}
                String[] parts = metaKey.split(":");
                String realFileId = parts[2];
                Long userId = Long.parseLong(parts[3]);

                // 3. 删除 Redis 中的列表和元信息
                String chunkListKey = buildChunkListKey(realFileId, userId);
                cacheService.delete(chunkListKey);
                cacheService.delete(metaKey);
                cacheService.delete("chunk:init:" + realFileId + ":" + userId);

                // 4. 删除 MinIO 上的所有临时分片
                String bucketName = StorageBucket.FILES.getBucketName();
                String prefix = "temp/" + realFileId + "/";
                List<String> objects = storageService.listObjectsByPrefix(bucketName, prefix);
                for (String objectName : objects) {
                    try {
                        storageService.deleteFile(bucketName, objectName);
                    } catch (Exception e) {
                        log.warn("删除 MinIO 分片失败: {}/{}", bucketName, objectName, e);
                    }
                }

                log.info("已清理过期分片 realFileId={}，userId={}", realFileId, userId);
            } catch (Exception e) {
                log.error("清理过期分片时发生异常，metaKey={}", metaKey, e);
            }
        }
    }

    /** 构造 Redis 分片列表 Key */
    private String buildChunkListKey(String realFileId, Long userId) {
        return "chunk:upload:" + realFileId + ":" + userId;
    }
}
