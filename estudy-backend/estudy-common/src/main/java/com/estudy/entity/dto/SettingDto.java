package com.estudy.entity.dto;

import lombok.Data;

@Data
public class SettingDto {

    private String adminPassword;
    // 扣子
    private String CozeToken;
    private String CozeWorkflowId;

}
