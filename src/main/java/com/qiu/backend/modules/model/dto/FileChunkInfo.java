package com.qiu.backend.modules.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileChunkInfo {

    Long totalChunks;

    String fileName;

    LocalDateTime updateTime;

    String fileId;
}
