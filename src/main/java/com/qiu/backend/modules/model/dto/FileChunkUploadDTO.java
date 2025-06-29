package com.qiu.backend.modules.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileChunkUploadDTO {

    @NotBlank(message = "文件ID不能为空")
    private String fileId;

    @NotNull(message = "分片编号不能为空")
    @Min(value = 1, message = "分片编号必须大于等于1")
    private Long chunkNumber;

    @NotNull(message = "总分片数不能为空")
    @Min(value = 1, message = "总分片数必须大于等于1")
    private Long totalChunks;

    @NotBlank(message = "文件名不能为空")
    private String filename;

    @NotNull(message = "文件分片不能为空")
    private MultipartFile chunk;
}
