package com.qiu.backend.common.core.constant;

import java.util.HashMap;
import java.util.Map;

public class RoleConstant {

    /**
     * 对应数据库的角色id
     */
    public static final Long ADMIN_ROLE_ID = 1L;

    public static final Long USER_ROLE_ID = 2L;

    public static final Long ADVANCED_ROLE_ID = 3L;

    public static final Long VISITOR_ROLE_ID = 4L;

    /**
     * 对于spring security的权限
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String ROLE_USER = "ROLE_USER";

    public static final String ROLE_ADVANCED = "ROLE_ADVANCED";

    public  static final String ROLE_VISITOR = "ROLE_VISITOR";

    /**
     * id 到 角色的转换
     */
    private static final Map<Long, String> idRole = new HashMap<>();

    static {
        idRole.put(ADMIN_ROLE_ID, ROLE_ADMIN);
        idRole.put(USER_ROLE_ID, ROLE_USER);
        idRole.put(ADVANCED_ROLE_ID, ROLE_ADVANCED);
        idRole.put(VISITOR_ROLE_ID, ROLE_VISITOR);
    }

    /**
     *
     * @param id 用户id
     * @return id对应的角色
     */
    public static String getRoleById(Long id) {
        return idRole.get(id);
    }

    // 判断是否存在id这个角色
    public static boolean existRole(Long id) {
        return idRole.containsKey(id);
    }
}
