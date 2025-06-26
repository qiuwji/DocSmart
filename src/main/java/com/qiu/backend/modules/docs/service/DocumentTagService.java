package com.qiu.backend.modules.docs.service;

import java.util.List;

public interface DocumentTagService {


    void insertBatch(Long documentId, List<Long> tagIds);
}
