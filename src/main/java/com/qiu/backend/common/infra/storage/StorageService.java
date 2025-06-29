package com.qiu.backend.common.infra.storage;

import io.minio.errors.*;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface StorageService {

    /**
     *
     * @param bucket 桶
     * @param inputStream 文件输入流
     * @param fileName 文件名
     * @return 访问的url
     * @throws Exception 异常
     */
    String uploadFile(String bucket, InputStream inputStream, String fileName, Long fileSize) throws Exception;

    /**
     * 获取文件访问的url
     * @param fileName 文件名
     * @return 访问url
     */
    String getFileUrl(String bucketName, String fileName) throws Exception;

    /**
     * 获取文件访问url
     * @param bucketName 桶名
     * @param fileName 文件名
     * @param expiry 过期时间，单位分钟
     * @return 文件 url
     * @throws Exception 异常
     */
    public String getTemporarilyFileUrl(String bucketName, String fileName, int expiry) throws Exception;

    /**
     * 删除文件
     * @param fileName 文件名
     */
    void deleteFile(String bucketName, String fileName) throws Exception;

    /**
     * 移动文件到另一个目录/位置
     * @param sourceBucket 源文件所在桶
     * @param sourceKey 源文件路径/键
     * @param targetBucket 目标桶(如果与源桶相同可省略)
     * @param targetKey 目标文件路径/键
     * @param overwrite 是否覆盖已存在的目标文件
     * @return 移动后文件的访问URL
     * @throws Exception 如果移动过程中出现错误
     */
    String moveFile(String sourceBucket, String sourceKey,
                    String targetBucket, String targetKey,
                    boolean overwrite) throws Exception;

    /**
     * 移动文件(在同一桶内移动)
     * @param bucket 桶名
     * @param sourceKey 源文件路径/键
     * @param targetKey 目标文件路径/键
     * @param overwrite 是否覆盖已存在的目标文件
     * @return 移动后文件的访问URL
     * @throws Exception 如果移动过程中出现错误
     */
    default String moveFile(String bucket, String sourceKey,
                            String targetKey, boolean overwrite) throws Exception {
        return moveFile(bucket, sourceKey, bucket, targetKey, overwrite);
    }

    /**
     * 列出指定 bucket 下，所有 objectName 以 prefix 开头的对象
     * @param bucketName 存储桶名称
     * @param prefix     对象名前缀
     * @return 对象名列表
     * @throws Exception
     */
    List<String> listObjectsByPrefix(String bucketName, String prefix) throws Exception;

    /**
     * 获取对象流
     * @param bucket 桶
     * @param objectName 对象名
     * @return 流
     */
    InputStream getObject(String bucket, String objectName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}

