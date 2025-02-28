package com.protectra.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.protectra")
@EnableMongoRepositories(basePackages = "com.protectra")
@EnableAsync
public class SharePointBackupApplication {

    public static void main(String[] args) {
        SpringApplication.run(SharePointBackupApplication.class, args);
    }
}