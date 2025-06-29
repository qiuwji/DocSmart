package com.qiu.backend.modules.docs.service;

import java.io.IOException;

public interface FileMergeService {

    void merge(String fileId, Long userId) throws IOException;
}
