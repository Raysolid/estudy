package com.estudy.admin.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.component.RedisComponent;
import com.estudy.entity.vo.Result;
import com.estudy.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AccountController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserService userService;

    /**
     * 验证码
     */
    @RequestMapping("/checkCode")
    public Result checkCode() {
        String checkCode = userService.checkCode();
        return Result.success(checkCode);
    }

    /**
     * 登录
     */
    @RequestMapping("/login")
    public Result login(@NotEmpty String username,
                        @NotEmpty String password,
                        @NotEmpty String checkCode) {
        String token = userService.loginAdmin(username, password, checkCode);
        return Result.success(token);
    }

    /**
     * 退出登录
     */
    @RequestMapping("/logout")
    @GlobalInterceptor(adminLogin = true)
    public Result logout(@RequestHeader(value = "token") String token) {
        redisComponent.cleanAdminToken(token);
        return Result.success(null);
    }

}