package com.qiu.backend.common.infra.storage;

import java.io.InputStream;

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
}

