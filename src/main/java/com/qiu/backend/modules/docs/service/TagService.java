package com.qiu.backend.modules.docs.service;

import java.util.List;

public interface TagService {

    /**
     * 获取标签id，如果不存在就创建
     * @param tags 标签名
     * @return
     */
    List<Long> getOrCreateTagIdsByName(List<String> tags, Long userId);
}
