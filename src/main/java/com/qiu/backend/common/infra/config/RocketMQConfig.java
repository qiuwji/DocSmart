package com.qiu.backend.common.infra.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;

@Slf4j
@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name-server}")
    private String nameServerAddr;

    @Value("${rocketmq.producer.group}")
    private String producerGroup;

    /**
     * 不在这里 start，交给 RocketMQTemplate.afterPropertiesSet() 去启动
     */
    @Bean
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServerAddr);
        // 不要 producer.start();
        return producer;
    }

    @Bean
    public RocketMQTemplate rocketMQTemplate(DefaultMQProducer producer) {
        RocketMQTemplate template = new RocketMQTemplate();
        template.setProducer(producer);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new ObjectMapper()
                .registerModule(new JavaTimeModule())  // 支持Java8时间类型
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));

        template.setMessageConverter(converter);
        log.info("RocketMQ初始化成功");
        return template;
    }
}
