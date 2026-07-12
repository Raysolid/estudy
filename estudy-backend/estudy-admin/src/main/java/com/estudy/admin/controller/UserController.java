package com.estudy.admin.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.entity.po.User;
import com.estudy.entity.vo.ExamReport;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.entity.vo.Result;
import com.estudy.service.ExamService;
import com.estudy.service.UserService;
import com.estudy.utils.StrUtils;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private ExamService examService;

    /**
     * 获取用户列表
     */
    @RequestMapping("/getUserList")
    @GlobalInterceptor(adminLogin = true)
    public Result getUserList(String userId, String email, String nickname,
                              @NotNull Integer pageNo, @NotNull Integer pageSize) {
        PaginationResult<User> result = userService.getUserList(userId, email, nickname, pageNo, pageSize);
        return Result.success(result);
    }

    /**
     * 新增/修改用户信息
     */
    @RequestMapping("/editUser")
    @GlobalInterceptor(adminLogin = true)
    public Result editUser(@NotNull User user) {
        Boolean isAdd = StrUtils.isEmpty(user.getUserId());
        userService.editUser(user, isAdd);
        return Result.success(null);
    }

    /**
     * 删除用户
     */
    @RequestMapping("/deleteUser")
    @GlobalInterceptor(adminLogin = true)
    public Result deleteUser(@NotEmpty String userId) {
        userService.deleteUser(userId);
        return Result.success(null);
    }

    /**
     * 获取考试记录
     */
    @RequestMapping("/getExamRecords")
    @GlobalInterceptor(adminLogin = true)
    public Result getExamRecords(String userId, Integer paperId,
                                 @NotNull Integer pageNo, @NotNull Integer pageSize) {
        PaginationResult<ExamReport> result = examService.getExamRecords(userId, paperId, pageNo, pageSize);
        return Result.success(result);
    }

    /**
     * 获取答题详情
     */
    @RequestMapping("/getRecordDetail")
    @GlobalInterceptor(adminLogin = true)
    public Result getRecordDetail(@NotNull Integer recordId) {
        ExamReport recordInfo = examService.getRecordInfo(recordId);
        return Result.success(recordInfo);
    }

}
