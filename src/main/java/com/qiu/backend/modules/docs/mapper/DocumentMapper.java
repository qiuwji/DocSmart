package com.qiu.backend.modules.docs.mapper;

import com.qiu.backend.modules.model.entity.Document;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocumentMapper {

    @Insert("INSERT INTO document " +
            "(user_id, folder_id, name, type, size, storage_path, status, deleted, create_time, update_time, original_filename) " +
            "VALUES " +
            "(#{userId}, #{folderId}, #{name}, #{type}, #{size}, #{storagePath}, #{status}, #{deleted}, #{createTime}, #{updateTime}, #{originalFilename})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Document document);

    @Select("SELECT * from document where id = #{id}")
    Document getById(Long id);
}
