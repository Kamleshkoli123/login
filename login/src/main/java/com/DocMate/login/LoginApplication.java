package com.DocMate.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.DocMate"})
public class LoginApplication {

    private static final Logger logger = LoggerFactory.getLogger(LoginApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Login Application...");
        SpringApplication.run(LoginApplication.class, args);
        logger.info("Login Application started successfully.");
    }
}
