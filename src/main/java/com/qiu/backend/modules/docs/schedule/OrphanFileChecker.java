package com.qiu.backend.modules.docs.schedule;

import com.qiu.backend.common.core.constant.FileConstant;
import com.qiu.backend.common.infra.cache.CacheService;
import com.qiu.backend.common.infra.storage.StorageBucket;
import com.qiu.backend.common.infra.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class OrphanFileChecker {

    private final StorageService storageService;

    private final CacheService cacheService;

    @Autowired
    public OrphanFileChecker(StorageService storageService,
                             CacheService cacheService) {
        this.storageService = storageService;
        this.cacheService = cacheService;
    }

    // 3600000 每小时检查
    @Scheduled(fixedDelay = 3600000)
    public void checkOrphanFiles() {
        log.info("开始执行孤立文件检查");
        String pattern = FileConstant.UPLOAD_IN_PROGRESS_PREFIX + "*";
        Set<String> keys = cacheService.keys(pattern);
        for (String key : keys) {
            String path = cacheService.get(key, String.class);
            if (path != null) {
                try {
                    storageService.deleteFile(StorageBucket.FILES.getBucketName(), path);
                } catch (Exception e) {
                    log.error("删除路径为:{}的文件失败", path);
                }
            }
            cacheService.delete(key);
        }
    }
}
