package com.qiu.backend.modules.user;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.security.SecurityConfig;
import com.qiu.backend.common.utils.UserContextHolder;
import com.qiu.backend.modules.model.dto.UserNameChangeDTO;
import com.qiu.backend.modules.user.controller.UserController;
import com.qiu.backend.modules.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static reactor.core.publisher.Mono.when;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class) // 导入安全配置
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // 使用注入的mockMvc或创建新的（二选一）
        // mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void changeUserName_Success() throws Exception {
        // Given
        Long userId = 1L;
        UserNameChangeDTO dto = new UserNameChangeDTO();
        dto.setUsername("newUsername");

        doNothing().when(userService).changeUserName(eq(userId), any(UserNameChangeDTO.class));

        UserContextHolder.setUserId(userId);

        // When & Then
        mockMvc.perform(patch("/api/user/{id}/username", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUsername\"}")) // 保持与DTO字段一致
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeUserName_Forbidden_WrongRole() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(patch("/api/user/{id}/username", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUsername\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeUserName_Unauthorized_NoAuth() throws Exception {
        // 测试不带认证的请求
        mockMvc.perform(patch("/api/user/1/username"))  // 这里补全了括号
                .andExpect(status().isUnauthorized());

        // 测试带内容但不带认证的请求
        mockMvc.perform(patch("/api/user/1/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newUsername\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void changeUserName_BadRequest_InvalidDTO() throws Exception {
        // 测试空请求体
        mockMvc.perform(patch("/api/user/1/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        // 测试无效字段
        mockMvc.perform(patch("/api/user/1/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"invalidField\":\"value\"}"))
                .andExpect(status().isBadRequest());
    }
}