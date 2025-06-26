package com.qiu.backend.modules.docs.mapper;

import com.qiu.backend.modules.model.entity.Folder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FolderMapper {

    Long createFolder(Folder folder);
}
