package com.qiu.backend.modules.user;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.modules.model.entity.UserRole;
import com.qiu.backend.modules.user.mapper.UserRoleMapper;
import com.qiu.backend.modules.user.service.impl.UserRoleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    @Test
    void createUserRole_WhenUserRoleNotExist_ShouldInsertSuccessfully() {
        // 准备测试数据
        UserRole userRole = new UserRole();
        userRole.setUserId(1L);
        userRole.setRoleId(2L);

        // 模拟行为
        when(userRoleMapper.existUserWithRole(userRole.getUserId(), userRole.getRoleId())).thenReturn(false);

        // 执行测试
        assertDoesNotThrow(() -> userRoleService.createUserRole(userRole));

        // 验证
        verify(userRoleMapper, times(1)).existUserWithRole(userRole.getUserId(), userRole.getRoleId());
        verify(userRoleMapper, times(1)).insert(userRole);
    }

    @Test
    void createUserRole_WhenUserRoleAlreadyExist_ShouldThrowBusinessException() {
        // 准备测试数据
        UserRole userRole = new UserRole();
        userRole.setUserId(1L);
        userRole.setRoleId(2L);

        // 模拟行为
        when(userRoleMapper.existUserWithRole(userRole.getUserId(), userRole.getRoleId())).thenReturn(true);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userRoleService.createUserRole(userRole));

        assertEquals(ResultCode.REGISTERED, exception.getErrorCode());

        // 验证
        verify(userRoleMapper, times(1)).existUserWithRole(userRole.getUserId(), userRole.getRoleId());
        verify(userRoleMapper, never()).insert(any());
    }

    @Test
    void existUserWithRole_ShouldReturnMapperResult() {
        // 准备测试数据
        Long userId = 1L;
        Long roleId = 2L;

        // 模拟行为
        when(userRoleMapper.existUserWithRole(userId, roleId)).thenReturn(true);

        // 执行测试
        boolean result = userRoleService.existUserWithRole(userId, roleId);

        // 验证
        assertTrue(result);
        verify(userRoleMapper, times(1)).existUserWithRole(userId, roleId);
    }
}