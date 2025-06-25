// 新建 DocumentStatus.java 文件
package com.qiu.backend.modules.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentStatus {
    UPLOADED("uploaded"),
    PARSING("parsing"),
    PARSED("parsed"),  // 注意这里修正了拼写错误(原代码是 parsed)
    FAILED("failed");

    private final String value;
}