package com.estudy.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.estudy"})
@MapperScan(basePackages = {"com.estudy.mappers"})
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class EStudyWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(EStudyWebApplication.class, args);
    }
}