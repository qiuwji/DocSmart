package com.qiu.backend.modules.user.controller;

import com.qiu.backend.common.core.Result.Result;
import com.qiu.backend.common.utils.UserContextHolder;
import com.qiu.backend.modules.model.dto.UserNameChangeDTO;
import com.qiu.backend.modules.model.vo.ChangeAvatarResponse;
import com.qiu.backend.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole(T(com.qiu.backend.common.core.constant.RoleConstant).ROLE_USER)")
    @PatchMapping("/{id}/username")
    public Result<Void> changeUserName(@PathVariable Long id, @Valid @RequestBody UserNameChangeDTO userNameChangeDTO) throws AccessDeniedException {
        log.info("用户：{}，开始更改名称", id);
        userService.changeUserName(id, userNameChangeDTO);
        return Result.success();
    }

    @PreAuthorize("hasRole(T(com.qiu.backend.common.core.constant.RoleConstant).ROLE_USER)")
    @PutMapping("/avatar")
    public ChangeAvatarResponse changeAvatar(@RequestParam("avatar") MultipartFile file) throws Exception {
        log.info("用户{}开始更改头像", UserContextHolder.getUserId());
        return userService.changeAvatar(file);
    }
}
