package com.qiu.backend.modules.docs.controller;

import com.qiu.backend.common.utils.UserContextHolder;
import com.qiu.backend.modules.docs.service.FolderService;
import com.qiu.backend.modules.model.dto.CreateFolderDTO;
import com.qiu.backend.modules.model.vo.CreateFolderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public CreateFolderResponse createFolder(@RequestBody CreateFolderDTO createFolderDTO) {
        Long userId = UserContextHolder.getUserId();
        log.info("用户{}开始创建目录", userId);

        Long id = folderService.createFolder(userId, createFolderDTO.getName(), createFolderDTO.getParentId());

        return new CreateFolderResponse(id, createFolderDTO.getName(), createFolderDTO.getParentId());
    }
}
