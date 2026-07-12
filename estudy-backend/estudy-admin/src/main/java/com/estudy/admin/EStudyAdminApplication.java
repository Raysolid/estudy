package com.estudy.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.estudy"})
@MapperScan(basePackages = {"com.estudy.mappers"})
@EnableTransactionManagement
@EnableScheduling
public class EStudyAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(EStudyAdminApplication.class, args);
    }
}