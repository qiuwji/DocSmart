package com.qiu.backend.modules.user.mapper;

import com.qiu.backend.modules.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    /**
     * 检查用户名是否存在
     * @param username 要检查的用户名
     * @return 存在返回 true，否则返回 false
     */
    boolean existByUsername(String username);

    /**
     * 检查用户名是否存在
     * @param email 要检查的邮箱
     * @return 存在返回 true，否则返回 false
     */
    boolean existByUserEmail(String email);

    /**
     * 插入新用户
     * @param user 要插入的用户
     * @return 受影响的行数
     */
    int insert(User user);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User getUserByEmail(String email);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User getUserByName(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User getUserById(Long id);

    int updateById(User user);
}
