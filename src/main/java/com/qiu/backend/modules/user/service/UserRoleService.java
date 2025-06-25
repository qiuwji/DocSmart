package com.qiu.backend.modules.user.service;

import com.qiu.backend.modules.model.dto.UserRoleChangeDTO;
import com.qiu.backend.modules.model.entity.UserRole;

import java.util.List;

public interface UserRoleService {

    List<Long> getUserRolesById(Long userId);

    void createUserRole(UserRole userRole);

    boolean existUserWithRole(Long userId, Long roleId);

    void changeUserRoles(Long userId, UserRoleChangeDTO userRoleChangeDTO);

    int deleteUserRoleById(Long userId);
}
