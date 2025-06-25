package com.qiu.backend.modules.user.controller;

import com.qiu.backend.common.core.Result.Result;
import com.qiu.backend.modules.model.dto.UserRoleChangeDTO;
import com.qiu.backend.modules.user.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user-role")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @Autowired
    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @PreAuthorize("hasRole(T(com.qiu.backend.common.core.constant.RoleConstant).ROLE_ADMIN)")
    @PutMapping("/change-role/{userId}/roles")
    public Result<Void> changeUserRoles(@PathVariable Long userId, @RequestBody UserRoleChangeDTO userRoleChangeDTO) {
        userRoleService.changeUserRoles(userId, userRoleChangeDTO);
        return Result.success();
    }
}
