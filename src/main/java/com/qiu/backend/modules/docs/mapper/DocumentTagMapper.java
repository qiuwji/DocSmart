package com.qiu.backend.modules.docs.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DocumentTagMapper {
    void insertBatch(@Param("documentId") Long documentId,
                     @Param("tagIds") List<Long> tagIds,
                     @Param("createTime") LocalDateTime createTime);
}
