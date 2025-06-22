package com.qiu.backend.modules.user.mapper;

import static org.mockito.Mockito.*;

import com.qiu.backend.modules.model.entity.UserRole;
import com.qiu.backend.modules.user.service.impl.UserRoleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceImplTest {

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    @Test
    void testCreateUserRole() {
        // 准备数据
        UserRole userRole = new UserRole();
        userRole.setUserId(1L);
        userRole.setRoleId(2L);

        // 模拟Mapper行为
        when(userRoleMapper.insert(userRole)).thenReturn(1); // 返回插入成功的行数

        // 执行测试
        userRoleService.createUserRole(userRole);

        // 验证Mapper方法被调用
        verify(userRoleMapper, times(1)).insert(userRole);
    }
}