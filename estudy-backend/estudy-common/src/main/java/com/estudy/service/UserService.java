package com.estudy.service;

import com.estudy.entity.dto.UserDto;
import com.estudy.entity.po.User;
import com.estudy.entity.vo.PaginationResult;

public interface UserService {

    /**
     * 获取验证码
     */
    String checkCode();

    /**
     * 获取邮箱验证码
     */
    void emailCode(String email);

    /**
     * 注册
     */
    void register(String email, String password, String emailCode);

    /**
     * 登录
     */
    UserDto loginByPassword(String email, String password);
    UserDto loginByEmailCode(String email, String emailCode);

    /**
     * 自动登录
     */
    UserDto autoLogin(String token);

    /**
     * 退出登录
     */
    void logout(String token);

    /**
     * 获取用户信息
     */
    UserDto getUserInfo(String userId);

    /**
     * 修改密码
     */
    void changePassword(String token, String password, String emailCode);

    /**
     * 修改用户信息
     */
    void updateUserInfo(String userId, String nickname, String preference);

    /**
     * 用户签到
     */
    void sign(String userId);

    /**
     * 管理端登录
     */
    String loginAdmin(String username, String password, String checkCode);

    /**
     * 管理端获取用户列表
     */
    PaginationResult<User> getUserList(String userId, String email, String nickname, Integer pageNo, Integer pageSize);

    /**
     * 新增/修改用户信息
     */
    void editUser(User user, Boolean isAdd);

    /**
     * 删除用户
     */
    void deleteUser(String userId);

}
