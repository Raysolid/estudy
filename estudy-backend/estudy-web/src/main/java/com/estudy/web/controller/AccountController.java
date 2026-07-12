package com.estudy.web.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.entity.dto.UserDto;
import com.estudy.entity.enums.ResultCode;
import com.estudy.entity.vo.Result;
import com.estudy.exception.BusinessException;
import com.estudy.service.UserService;
import com.estudy.utils.StrUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@Validated
public class AccountController {

    @Resource
    private UserService userService;

    /**
     * 邮箱验证码
     */
    @RequestMapping("/emailCode")
    public Result emailCode(@NotEmpty @Email String email) {
        userService.emailCode(email);
        return Result.success(null);
    }

    /**
     * 注册
     */
    @RequestMapping("/register")
    public Result register(@NotEmpty @Email String email,
                           @NotEmpty String password,
                           @NotEmpty String emailCode) {
        userService.register(email, password, emailCode);
        return Result.success(null);
    }

    /**
     * 登录
     */
    @RequestMapping("/login")
    public Result login(@NotEmpty @Email String email,
                        String password,
                        String emailCode) {
        if (password == null && emailCode == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        UserDto userDto = null;
        if (password != null && !StrUtils.isEmpty(password)) { // 密码登录
            userDto = userService.loginByPassword(email, password);
        } else { // 验证码登录
            userDto = userService.loginByEmailCode(email, emailCode);
        }
        return Result.success(userDto);
    }

    /**
     * 自动登录
     */
    @RequestMapping("/autoLogin")
    public Result autoLogin(@RequestHeader(value = "token") String token) {
        UserDto userDto = userService.autoLogin(token);
        return Result.success(userDto);
    }

    /**
     * 退出登录
     */
    @RequestMapping("/logout")
    @GlobalInterceptor
    public Result logout(@RequestHeader(value = "token") String token) {
        userService.logout(token);
        return Result.success(null);
    }

    /**
     * 修改密码
     */
    @RequestMapping("/changePassword")
    @GlobalInterceptor
    public Result changePassword(@RequestHeader(value = "token") String token,
                                 @NotEmpty String password,
                                 @NotEmpty String emailCode) {
        userService.changePassword(token, password, emailCode);
        return Result.success(null);
    }

}