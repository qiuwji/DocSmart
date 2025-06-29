package com.qiu.backend.modules.model.dto;

import lombok.Data;

@Data
public class CreateFolderDTO {

    String name;

    Long parentId;
}
