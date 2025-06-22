package com.qiu.backend.modules.user.service.impl;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.modules.model.entity.User;
import com.qiu.backend.modules.user.mapper.UserMapper;
import com.qiu.backend.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public int createUser(User user) {
        // 判断是否已经创建
        if (existByUserEmail(user.getEmail())) {
            throw new BusinessException(ResultCode.REGISTERED);
        }

        return userMapper.insert(user);
    }

    @Override
    public boolean existByUserName(String username) {
        return userMapper.existByUsername(username);
    }

    @Override
    public boolean existByUserEmail(String email) {
        return userMapper.existByUserEmail(email);
    }
}
