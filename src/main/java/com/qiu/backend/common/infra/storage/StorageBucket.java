package com.qiu.backend.common.infra.storage;

import lombok.Getter;

@Getter
public enum StorageBucket {

    AVATARS("avatars"),
    FILES("files");

    private final String bucketName;

    StorageBucket(String bucketName) {
        this.bucketName = bucketName;
    }

}
