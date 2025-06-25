package com.qiu.backend.modules.user;


import com.qiu.backend.common.core.constant.RoleConstant;
import com.qiu.backend.modules.model.entity.User;
import com.qiu.backend.modules.user.service.UserRoleService;
import com.qiu.backend.modules.user.service.UserService;
import com.qiu.backend.modules.user.service.impl.CustomUserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private CustomUserDetailsServiceImpl customUserDetailsService;

    private final String USERNAME = "testUser";
    private final String PASSWORD = "encodedPassword";
    private final Long USER_ID = 1L;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setUsername(USERNAME);
        testUser.setPassword(PASSWORD);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // 准备测试数据
        List<Long> roleIds = Arrays.asList(1L, 2L);

        // 模拟行为
        when(userService.getUserById(USERNAME)).thenReturn(testUser);
        when(userRoleService.getUserRolesById(USER_ID)).thenReturn(roleIds);

        // 使用真实 RoleConstant 方法，或者准备测试数据使其返回预期值
        // 假设 RoleConstant.getRoleById(1L) 返回 "ROLE_ADMIN"
        // 假设 RoleConstant.getRoleById(2L) 返回 "ROLE_USER"

        // 执行测试
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(USERNAME);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(USERNAME, userDetails.getUsername());
        assertEquals(PASSWORD, userDetails.getPassword());

        // 验证角色是否正确转换
        // 这里我们只验证权限数量，或者根据 RoleConstant 的实际行为验证
        assertEquals(2, userDetails.getAuthorities().size());

        // 或者如果需要验证具体角色，可以这样：
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> RoleConstant.getRoleById(1L).equals(auth.getAuthority())));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> RoleConstant.getRoleById(2L).equals(auth.getAuthority())));

        // 验证mock交互
        verify(userService).getUserById(USERNAME);
        verify(userRoleService).getUserRolesById(USER_ID);
    }


    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserNotExists() {
        // 模拟行为
        when(userService.getUserById(USERNAME)).thenReturn(null);

        // 执行测试并验证异常
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(USERNAME);
        });

        // 验证mock交互
        verify(userService).getUserById(USERNAME);
        verifyNoInteractions(userRoleService);
    }

    @Test
    void loadUserByUsername_ShouldHandleEmptyRoles() {
        // 准备测试数据 - 无角色
        List<Long> emptyRoleIds = Collections.emptyList();

        // 模拟行为
        when(userService.getUserById(USERNAME)).thenReturn(testUser);
        when(userRoleService.getUserRolesById(USER_ID)).thenReturn(emptyRoleIds);

        // 执行测试
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(USERNAME);

        // 验证结果
        assertNotNull(userDetails);
        assertEquals(USERNAME, userDetails.getUsername());
        assertEquals(PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());

        // 验证mock交互
        verify(userService).getUserById(USERNAME);
        verify(userRoleService).getUserRolesById(USER_ID);
    }
}