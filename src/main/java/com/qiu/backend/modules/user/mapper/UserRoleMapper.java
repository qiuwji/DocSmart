package com.qiu.backend.modules.user.mapper;

import com.qiu.backend.modules.model.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    @Select("SELECT COUNT(1) > 0 FROM user_role ur " +
            "WHERE ur.user_id = #{userId} AND ur.role_id = #{roleId}")
    boolean existUserWithRole(Long userId, Long roleId);

    @Insert("INSERT INTO user_role (user_id, role_id, create_time) VALUES " +
            "(#{userId}, #{roleId}, #{createTime})")
    int insert(UserRole userRole);

    @Select("SELECT user_role.role_id from user_role where user_id = #{userId}")
    List<Long> getUserRolesById(Long userId);

    @Select("SELECT EXISTS(SELECT 1 FROM user_role where user_id = #{userId})")
    boolean existUser(Long userId);

    @Delete("DELETE FROM user_role where user_id = #{userId}")
    int deleteUserRoleById(Long userId);
}
