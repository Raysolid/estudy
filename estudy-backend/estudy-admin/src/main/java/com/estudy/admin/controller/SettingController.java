package com.estudy.admin.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.component.RedisComponent;
import com.estudy.entity.dto.SettingDto;
import com.estudy.entity.vo.Result;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class SettingController {

    @Resource
    private RedisComponent redisComponent;

    /**
     * 获取系统设置
     */
    @RequestMapping("/getSetting")
    @GlobalInterceptor(adminLogin = true)
    public Result getSetting() {
        SettingDto setting = redisComponent.getSetting();
        return Result.success(setting);
    }

    /**
     * 保存系统设置
     */
    @RequestMapping("/saveSetting")
    @GlobalInterceptor(adminLogin = true)
    public Result saveSetting(@NotNull SettingDto settingDto) {
        redisComponent.saveSetting(settingDto);
        return Result.success(null);
    }

}
