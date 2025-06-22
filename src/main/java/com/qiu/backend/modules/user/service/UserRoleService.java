package com.qiu.backend.modules.user.service;

import com.qiu.backend.modules.model.entity.UserRole;

public interface UserRoleService {
    void createUserRole(UserRole userRole);

    boolean existUserWithRole(Long userId, Long roleId);
}
