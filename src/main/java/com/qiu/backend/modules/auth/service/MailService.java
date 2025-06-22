package com.qiu.backend.modules.auth.service;

public interface MailService {

    void sendSimpleMail(String to, String subject, String content);
}
