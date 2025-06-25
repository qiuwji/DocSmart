package com.qiu.backend.modules.user.service.impl;

import com.qiu.backend.common.core.constant.RoleConstant;
import com.qiu.backend.modules.model.entity.User;
import com.qiu.backend.modules.user.service.UserRoleService;
import com.qiu.backend.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    private UserDetails getDetails(User user) {
        if (user == null) {
            throw new UsernameNotFoundException("用户未找到");
        }

        List<Long> userRoleIds = userRoleService.getUserRolesById(user.getId());

        List<String> userRoles = new ArrayList<>();

        for (Long roleId : userRoleIds) {
            userRoles.add(RoleConstant.getRoleById(roleId));
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),                   // 用户名
                user.getPassword(),                   // 密码（已加密）
                AuthorityUtils.createAuthorityList(userRoles.toArray(new String[0])) // 权限
        );
    }

    public UserDetails loadUserByUserId(Long id) {
        User user = userService.getUserById(id);

        return getDetails(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserById(username);

        return getDetails(user);
    }
}
