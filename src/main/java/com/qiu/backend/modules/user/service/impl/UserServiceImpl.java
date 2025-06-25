package com.qiu.backend.modules.user.service.impl;

import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.constant.FileConstant;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.common.infra.storage.StorageBucket;
import com.qiu.backend.common.infra.storage.StorageService;
import com.qiu.backend.common.utils.FileUtil;
import com.qiu.backend.common.utils.RandomUtil;
import com.qiu.backend.common.utils.UserContextHolder;
import com.qiu.backend.modules.model.dto.UserNameChangeDTO;
import com.qiu.backend.modules.model.entity.User;
import com.qiu.backend.modules.model.vo.ChangeAvatarResponse;
import com.qiu.backend.modules.user.mapper.UserMapper;
import com.qiu.backend.modules.user.service.UserService;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import static com.qiu.backend.common.core.constant.UserConstant.DEFAULT_AVATAR_NAME;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final StorageService storageService;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, StorageService storageService) {
        this.userMapper = userMapper;
        this.storageService = storageService;
    }

    @Override
    public User getUserById(String username) {
        User user = userMapper.getUserByName(username);

        if (user == null) {
            throw new UsernameNotFoundException("用户未找到");
        }
        return user;
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.getUserById(id);

        if (user == null) {
            throw new UsernameNotFoundException("用户未找到");
        }
        return user;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeUserName(Long id, UserNameChangeDTO userNameChangeDTO) throws AccessDeniedException {
        // 1. 权限校验
        if (!UserContextHolder.getUserId().equals(id)) {
            throw new AccessDeniedException("不能修改别人的用户名");
        }

        // 2. 参数校验
        if (StringUtils.isEmpty(userNameChangeDTO.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        // 3. 唯一性校验
        if (userMapper.existByUsername(userNameChangeDTO.getUsername())) {
            throw new AccessDeniedException("用户名已存在");
        }

        // 4. 构建更新对象
        User user = new User();
        user.setId(id);
        user.setUsername(userNameChangeDTO.getUsername());
        user.setUpdateTime(LocalDateTime.now());

        // 5. 执行更新
        userMapper.updateById(user);

        log.debug("用户ID：{} 修改用户名成功", id);
    }

    @Override
    @Transactional
    public ChangeAvatarResponse changeAvatar(MultipartFile file) throws Exception {
        // 1. 校验文件
        validateAvatarFile(file);

        // 2. 生成安全文件名
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.getExtension(originalFilename);
        String fileName = RandomUtil.randomUUID() + extension;

        Long userId = UserContextHolder.getUserId();

        String bucket = StorageBucket.AVATARS.getBucketName();

        String tempPath = "temp/" + fileName;

        try {
            // 3. 上传到临时目录
            storageService.uploadFile(bucket, file.getInputStream(), tempPath, file.getSize());

            // 4. 移动文件到正式目录
            storageService.moveFile(bucket, tempPath,
                    bucket, fileName, true);

            // 5. 清理旧文件并更新数据库
            cleanupOldAvatar(userId);
            updateUserAvatar(userId, fileName);

            return new ChangeAvatarResponse(fileName);
        } catch (Exception e) {
            log.error("头像更新失败: userId={}", userId, e);
            storageService.deleteFile(bucket, tempPath);
            throw new BusinessException(ResultCode.FAILED);
        }
    }

    private void cleanupOldAvatar(Long userId) {
        User user = userMapper.getUserById(userId);
        String oldAvatarFileName = user.getAvatar();

        // 2. 如果存在旧头像，则删除
        if (oldAvatarFileName != null && !oldAvatarFileName.isEmpty()) {

            if (oldAvatarFileName.equals(DEFAULT_AVATAR_NAME)) {
                return;
            }

            try {
                storageService.deleteFile(
                        StorageBucket.AVATARS.getBucketName(),
                        oldAvatarFileName
                );
                log.info("已清理用户旧头像: userId={}, filename={}", userId, oldAvatarFileName);
            } catch (Exception e) {
                log.error("清理旧头像失败: userId={}", userId, e);
                // 失败不影响主流程，可加入重试或告警机制
            }
        }
    }

    private void updateUserAvatar(Long userId, String filename) {
        User user = new User();
        user.setAvatar(filename);
        user.setUpdateTime(LocalDateTime.now());
        user.setId(userId);;
        userMapper.updateById(user);
    }

    private void validateAvatarFile(MultipartFile file) {
        if (!FileUtil.isImage(file)) {
            throw new IllegalArgumentException("头像必须是JPG/PNG格式");
        }
        if (!FileUtil.isSizeValid(file, FileConstant.AVATAR_MAX_MB)) {
            throw new IllegalArgumentException(
                    String.format("头像大小不能超过 %dMB", FileConstant.AVATAR_MAX_MB)
            );
        }
    }
}
