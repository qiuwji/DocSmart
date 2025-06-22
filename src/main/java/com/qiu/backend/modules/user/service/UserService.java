package com.qiu.backend.modules.user.service;

import com.qiu.backend.modules.model.entity.User;

public interface UserService {

    int createUser(User user);

    boolean existByUserName(String username);

    boolean existByUserEmail(String email);
}
