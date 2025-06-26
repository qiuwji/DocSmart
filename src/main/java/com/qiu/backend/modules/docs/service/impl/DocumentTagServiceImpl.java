package com.qiu.backend.modules.docs.service.impl;

import com.qiu.backend.modules.docs.mapper.DocumentTagMapper;
import com.qiu.backend.modules.docs.service.DocumentTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentTagServiceImpl implements DocumentTagService {

    private final DocumentTagMapper documentTagMapper;

    @Autowired
    public DocumentTagServiceImpl(DocumentTagMapper documentTagMapper) {
        this.documentTagMapper = documentTagMapper;
    }

    @Override
    public void insertBatch(Long documentId, List<Long> tagIds) {
        if (documentId == null || tagIds == null || tagIds.isEmpty()) {
            return;
        }

        LocalDateTime createTime = LocalDateTime.now();
        documentTagMapper.insertBatch(documentId, tagIds, createTime);
    }
}
