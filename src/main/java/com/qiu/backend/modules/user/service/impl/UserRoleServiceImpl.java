package com.qiu.backend.modules.user.service.impl;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.modules.model.entity.UserRole;
import com.qiu.backend.modules.user.mapper.UserRoleMapper;
import com.qiu.backend.modules.user.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleMapper userRoleMapper;

    @Autowired
    public UserRoleServiceImpl(UserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public void createUserRole(UserRole userRole) {
        if (existUserWithRole(userRole.getUserId(), userRole.getRoleId())) {
            throw new BusinessException(ResultCode.REGISTERED);
        }

        userRoleMapper.insert(userRole);
    }

    @Override
    public boolean existUserWithRole(Long userId, Long roleId) {
        return userRoleMapper.existUserWithRole(userId, roleId);
    }
}
