package com.qiu.backend.modules.docs.service;

public interface FolderService {

    /**
     * 创建目录
     * @param userId 用户id
     * @param folderName 目录名
     * @param parentId 父目录id
     * @return 创建后的目录id
     */
    Long createFolder(Long userId, String folderName, Long parentId);
}
