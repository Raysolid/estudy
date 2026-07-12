package com.estudy.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppConfig {

    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${spring.mail.username:}")
    private String sendCodeEmail;

    @Value("${admin.account:}")
    private String adminAccount;

    @Value("${admin.password:}")
    public String adminPassword;

    @Value("${coze.api.upload-url:}")
    private String uploadUrl;

    @Value("${coze.api.workflow-url:}")
    private String workflowUrl;

}
