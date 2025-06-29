package com.qiu.backend.modules.mq.producer;

import com.qiu.backend.common.core.exception.MessageSendException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageProducer {

    private final RocketMQTemplate rocketMQTemplate;

    private static final int DEFAULT_TIMEOUT = 3000; // 默认发送超时3秒

    @Autowired
    public MessageProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 发送普通消息
     * @param topic 主题
     * @param payload 消息体
     * @param <T> 消息类型
     * @return 发送结果
     */
    public <T> SendResult send(String topic, T payload) {
        try {
            Message<T> message = MessageBuilder.withPayload(payload).build();
            return rocketMQTemplate.syncSend(topic, message, DEFAULT_TIMEOUT);
        } catch (Exception e) {
            throw new MessageSendException("消息发送失败", e, topic, payload);
        }
    }

    /**
     * 发送带标签的消息
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息体
     * @param <T> 消息类型
     * @return 发送结果
     */
    public <T> SendResult sendWithTag(String topic, String tag, T payload) {
        try {
            String destination = String.format("%s:%s", topic, tag);
            return rocketMQTemplate.syncSend(destination, payload, DEFAULT_TIMEOUT);
        } catch (Exception e) {
            throw new MessageSendException("带标签消息发送失败", e, topic, payload);
        }
    }

    /**
     * 发送延迟消息
     * @param topic 主题
     * @param payload 消息体
     * @param delay 延迟时间
     * @param timeUnit 时间单位
     * @param <T> 消息类型
     * @return 发送结果
     */
    public <T> SendResult sendDelayed(String topic, T payload, long delay, TimeUnit timeUnit) {
        try {
            int delayLevel = convertToDelayLevel(delay, timeUnit);
            return rocketMQTemplate.syncSend(topic,
                    MessageBuilder.withPayload(payload).build(),
                    DEFAULT_TIMEOUT,
                    delayLevel);
        } catch (Exception e) {
            throw new MessageSendException("延迟消息发送失败", e, topic, payload);
        }
    }

    // RocketMQ延迟级别转换
    private int convertToDelayLevel(long delay, TimeUnit timeUnit) {
        long seconds = timeUnit.toSeconds(delay);
        if (seconds <= 1) return 1;
        if (seconds <= 5) return 2;
        if (seconds <= 10) return 3;
        if (seconds <= 30) return 4;
        if (seconds <= 60) return 5;
        // 更多级别转换...
        return 1; // 默认
    }

    /**
     * 异步发送消息
     * @param topic 主题
     * @param payload 消息体
     * @param callback 回调函数
     * @param <T> 消息类型
     */
    public <T> void asyncSend(String topic, T payload, SendCallback callback) {
        try {
            rocketMQTemplate.asyncSend(topic, payload, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.trace("消息发送成功: {}", sendResult);
                    callback.onSuccess(sendResult);
                }

                @Override
                public void onException(Throwable e) {
                    log.error("消息发送失败", e);
                    callback.onException(e);
                }
            });
        } catch (Exception e) {
            throw new MessageSendException("异步消息发送失败", e, topic, payload);
        }
    }

    /**
     * 发送事务消息
     * @param topic 主题
     * @param payload 消息体
     * @param arg 事务参数
     * @param <T> 消息类型
     * @return 发送结果
     */
    public <T> SendResult sendTransactional(String topic, T payload, Object arg) {
        try {
            return rocketMQTemplate.sendMessageInTransaction(
                    topic,
                    MessageBuilder.withPayload(payload).build(),
                    arg
            );
        } catch (Exception e) {
            throw new MessageSendException("事务消息发送失败", e, topic, payload);
        }
    }

    /**
     * 批量发送消息
     * @param topic 主题
     * @param payloads 消息列表
     * @param <T> 消息类型
     */
    public <T> void sendBatch(String topic, List<T> payloads) {
        try {
            List<Message<T>> messages = payloads.stream()
                    .map(payload -> MessageBuilder.withPayload(payload).build())
                    .collect(Collectors.toList());

            rocketMQTemplate.syncSend(topic, messages, DEFAULT_TIMEOUT);
        } catch (Exception e) {
            throw new MessageSendException("批量消息发送失败", e, topic, payloads);
        }
    }
}
