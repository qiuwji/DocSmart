package com.qiu.backend.modules.auth.service.impl;

import com.qiu.backend.modules.auth.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Autowired
    public MailServiceImpl(JavaMailSender javaMailSender) {
        this.mailSender = javaMailSender;
    }

    @Value("${spring.mail.username}")
    private String from;

    public void sendSimpleMail(String to, String subject, String content) {
        log.info("发送邮件到 + {}，主题是: + {}", to, subject);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);      // 发件人
        message.setTo(to);          // 收件人
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}
