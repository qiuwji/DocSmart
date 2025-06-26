package com.qiu.backend.common.core.constant;

public class FileConstant {

    /**
     * 头像大小支持
     */
    public static final int AVATAR_MAX_MB = 3;

    /**
     * 文件大小支持
     */
    public static final int FILE_MAX_MB = 20;

    /**
     * 最多能传的tags数目
     */
    public static final int MAX_TAGS_COUNT = 7;

    /*
    上传失败的key是storage:file_type:name
           value是:pending
     */
    public static final String UPLOAD_IN_PROGRESS_PREFIX = "storage:";

    public static final String PDF_PATH = "pdf";

    public static final String WORD_PATH = "word";

    public static final String TXT_PATH = "txt";

    public static final String UPLOAD_IN_PROGRESS_VALUE = "pending";
}
