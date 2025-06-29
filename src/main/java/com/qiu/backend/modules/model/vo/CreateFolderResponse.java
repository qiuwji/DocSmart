package com.qiu.backend.modules.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateFolderResponse {

    Long id;

    String name;

    Long parentId;
}
