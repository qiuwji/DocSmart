package com.qiu.backend.modules.docs.service.impl;

import com.qiu.backend.modules.docs.mapper.FolderMapper;
import com.qiu.backend.modules.docs.service.FolderService;
import com.qiu.backend.modules.model.entity.Folder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class FolderServiceImpl implements FolderService {

    private final FolderMapper folderMapper;

    @Autowired
    public FolderServiceImpl(FolderMapper folderMapper) {
        this.folderMapper = folderMapper;
    }

    @Override
    public Long createFolder(Long userId, String folderName, Long parentId) {
        if (userId == null || !StringUtils.hasText(folderName) || parentId == null) {
            throw new IllegalArgumentException("参数校验失败");
        }

        Folder folder = new Folder();
        folder.setName(folderName);
        folder.setUserId(userId);
        folder.setParentId(parentId);

        LocalDateTime now = LocalDateTime.now();
        folder.setCreateTime(now);
        folder.setUpdateTime(now);

        return folderMapper.createFolder(folder);
    }
}
