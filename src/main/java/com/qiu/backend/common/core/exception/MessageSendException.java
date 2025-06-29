package com.qiu.backend.common.core.exception;

import lombok.Getter;

/**
 * 消息发送异常
 */
@Getter
public class MessageSendException extends RuntimeException {
    private final String topic;
    private final Object payload;

    public MessageSendException(String message, Throwable cause, String topic, Object payload) {
        super(message, cause);
        this.topic = topic;
        this.payload = payload;
    }

}
