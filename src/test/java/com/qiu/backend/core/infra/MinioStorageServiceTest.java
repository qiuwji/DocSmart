package com.qiu.backend.core.infra;

import com.qiu.backend.common.infra.storage.impl.MinioStorageService;
import io.minio.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioStorageService minioStorageService;

    private final String testBucket = "test-bucket";
    private final String testFileName = "test-file.txt";
    private final String testUrl = "http://minio.example.com/test-bucket/test-file.txt";
    private final long testFileSize = 1024L;

    @Test
    void uploadFile_Success() throws Exception {
        // 准备测试数据
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        // 模拟行为
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(testUrl);

        // 调用方法
        String resultUrl = minioStorageService.uploadFile(testBucket, inputStream, testFileName, testFileSize);

        // 验证结果
        assertEquals(testUrl, resultUrl);
        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void getFileUrl_InvalidParameters_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> minioStorageService.getFileUrl("", testFileName));
        assertThrows(IllegalArgumentException.class, () -> minioStorageService.getFileUrl(testBucket, ""));
    }

    @Test
    void getTemporarilyFileUrl_InvalidExpiry_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> minioStorageService.getTemporarilyFileUrl(testBucket, testFileName, 0));
        assertThrows(IllegalArgumentException.class,
                () -> minioStorageService.getTemporarilyFileUrl(testBucket, testFileName, -1));
    }

    @Test
    void deleteFile_Success() throws Exception {
        // 模拟行为
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        // 调用方法
        minioStorageService.deleteFile(testBucket, testFileName);

        // 验证
        verify(minioClient).removeObject(argThat(args ->
                args.bucket().equals(testBucket) &&
                        args.object().equals(testFileName)
        ));
    }

    @Test
    void deleteFile_InvalidParameters_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> minioStorageService.deleteFile("", testFileName));
        assertThrows(IllegalArgumentException.class, () -> minioStorageService.deleteFile(testBucket, ""));
    }

}
