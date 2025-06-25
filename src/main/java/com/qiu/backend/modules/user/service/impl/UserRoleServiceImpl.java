package com.qiu.backend.modules.user.service.impl;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.constant.RoleConstant;
import com.qiu.backend.common.core.constant.UserConstant;
import com.qiu.backend.common.core.exception.ApiException;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.common.infra.cache.impl.RedisCacheService;
import com.qiu.backend.modules.model.dto.UserRoleChangeDTO;
import com.qiu.backend.modules.model.entity.UserRole;
import com.qiu.backend.modules.user.mapper.UserRoleMapper;
import com.qiu.backend.modules.user.service.UserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleMapper userRoleMapper;

    private final RedisCacheService cacheService;

    @Autowired
    public UserRoleServiceImpl(UserRoleMapper userRoleMapper, RedisCacheService cacheService) {
        this.userRoleMapper = userRoleMapper;
        this.cacheService = cacheService;
    }

    @Override
    public List<Long> getUserRolesById(Long userId) {
        List<Long> userRoles = userRoleMapper.getUserRolesById(userId);

        if (userRoles == null || userRoles.isEmpty()) {
            throw new UsernameNotFoundException("未找到该用户");
        }

        return userRoles;
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

    private boolean existUser(Long userId) {
        return userRoleMapper.existUser(userId);
    }

    @Override
    @Transactional
    public void changeUserRoles(Long userId, UserRoleChangeDTO userRoleChangeDTO) {
        log.info("用户id为：{}的用户权限尝试变更", userId);
        // 判断用户是否存在
        if (!existUser(userId)) {
            throw new ApiException(ResultCode.USER_NOT_FOUND);
        }

        if (userRoleChangeDTO == null || userRoleChangeDTO.getRoleIds().isEmpty()) {
            throw new ApiException(ResultCode.FAILED);
        }

        for (Long roleId : userRoleChangeDTO.getRoleIds()) {
            if (!RoleConstant.existRole(roleId)) {
                throw new BusinessException(ResultCode.FAILED, "角色不存在");
            }
        }

        deleteUserRoleById(userId);

        for (Long roleId : userRoleChangeDTO.getRoleIds()) {
            userRoleMapper.insert(new UserRole(userId, roleId, LocalDateTime.now()));
        }

        // 是否踢人下线
        if (userRoleChangeDTO.isForceLogout()) {
            String key = UserConstant.TOKEN_VALID_PREFIX + userId;
            cacheService.delete(key);
        }
    }

    @Override
    public int deleteUserRoleById(Long userId) {
        int count = userRoleMapper.deleteUserRoleById(userId);

        if (count == 0) {
            // 代表用户不存在
            throw new ApiException(ResultCode.USER_NOT_FOUND);
        }

        return count;
    }
}
