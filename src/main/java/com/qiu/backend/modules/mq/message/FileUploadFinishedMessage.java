package com.qiu.backend.modules.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadFinishedMessage {

    String fileId;

    Long userId;
}
