package com.qiu.backend.modules.docs.mapper;

import com.qiu.backend.modules.model.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface TagMapper {

    /**
     * 根据 userId 和 tag 名称列表，批量查询已存在的 Tag
     */
    List<Tag> selectByNamesAndUserId(@Param("names") List<String> names,
                                     @Param("userId") Long userId);

    /**
     * 插入一个新的 Tag，并返回主键（useGeneratedKeys）
     */
    int insert(Tag tag);
}
