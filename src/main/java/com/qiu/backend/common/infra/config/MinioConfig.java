package com.qiu.backend.common.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import io.minio.MinioClient;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Autowired
    public MinioConfig(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }

    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient client = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();

            // 验证连接是否成功
            client.listBuckets(); // 简单操作测试连接

            log.info("MinIO客户端初始化成功 - 连接到 {}", minioProperties.getEndpoint());
            return client;
        } catch (Exception e) {
            log.error("MinIO客户端初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("MinIO客户端初始化失败", e);
        }
    }
}
