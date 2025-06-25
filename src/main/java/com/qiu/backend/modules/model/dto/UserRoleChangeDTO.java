package com.qiu.backend.modules.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserRoleChangeDTO {

    private List<Long> roleIds;

    private boolean forceLogout;
}
