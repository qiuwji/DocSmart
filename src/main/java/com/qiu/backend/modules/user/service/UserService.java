package com.qiu.backend.modules.user.service;

import com.qiu.backend.modules.model.dto.UserNameChangeDTO;
import com.qiu.backend.modules.model.entity.User;
import com.qiu.backend.modules.model.vo.ChangeAvatarResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

public interface UserService {

    User getUserById(String username);

    User getUserById(Long id);

    int createUser(User user);

    boolean existByUserName(String username);

    boolean existByUserEmail(String email);

    void changeUserName(Long id, UserNameChangeDTO userNameChangeDTO) throws AccessDeniedException;

    ChangeAvatarResponse changeAvatar(MultipartFile file) throws Exception;
}
