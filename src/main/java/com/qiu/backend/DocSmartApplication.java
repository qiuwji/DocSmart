package com.qiu.backend;

import com.qiu.backend.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class DocSmartApplication implements CommandLineRunner {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public static void main(String[] args) {
        SpringApplication.run(DocSmartApplication.class, args);
    }

    @Override
    public void run(String... args) {
        JwtUtil.init(jwtSecret, jwtExpiration);
    }

}
