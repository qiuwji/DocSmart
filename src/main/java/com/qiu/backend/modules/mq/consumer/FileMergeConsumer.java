package com.qiu.backend.modules.mq.consumer;

import com.qiu.backend.modules.docs.service.FileMergeService;
import com.qiu.backend.modules.mq.message.FileUploadFinishedMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "file-merge-topic", consumerGroup = "file-merge-group")
public class FileMergeConsumer implements RocketMQListener<FileUploadFinishedMessage> {

    private final FileMergeService fileMergeService;

    @Autowired
    public FileMergeConsumer(FileMergeService fileMergeService) {
        this.fileMergeService = fileMergeService;
    }

    @Override
    public void onMessage(FileUploadFinishedMessage message) {
        log.info("Received message from file-merge-topic: {}", message);
        try {

            String fileId = message.getFileId();
            Long userId = message.getUserId();

            // 调用合并逻辑
            fileMergeService.merge(fileId, userId);
            log.info("File merge successful for fileId={}, userId={}", fileId, userId);
        } catch (Exception e) {
            log.error("File merge failed for message: {}", message, e);
            // 根据业务，可以重试或记录失败日志
        }
    }
}
