package com.estudy.web.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.component.RedisComponent;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.entity.vo.Result;
import com.estudy.entity.vo.WrongQuestionInfo;
import com.estudy.service.WrongQuestionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wrong")
@Validated
public class WrongQuestionController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private WrongQuestionService wrongQuestionService;

    /**
     * 最近错题
     */
    @RequestMapping("/getRecentWrongQuestions")
    @GlobalInterceptor
    public Result getRecentWrongQuestions(@RequestHeader(value = "token") String token) {
        String userId = redisComponent.getUserToken(token).getUserId();
        List<WrongQuestionInfo> recentList = wrongQuestionService.getRecentWrongQuestions(userId);
        return Result.success(recentList);
    }

    /**
     * 获取用户错题
     */
    @RequestMapping("/getWrongQuestions")
    @GlobalInterceptor
    public Result getWrongQuestions(@RequestHeader(value = "token") String token,
                                    Integer pageNo) {
        String userId = redisComponent.getUserToken(token).getUserId();
        PaginationResult<WrongQuestionInfo> result = wrongQuestionService.getWrongQuestions(userId, pageNo);
        return Result.success(result);
    }

    /**
     * 根据id获取错题信息
     */
    @RequestMapping("/getWrongQuestionById")
    @GlobalInterceptor
    public Result getWrongQuestionById(@NotNull Integer questionId) {
        WrongQuestionInfo wrongQuestionInfo = wrongQuestionService.getWrongQuestionById(questionId);
        return Result.success(wrongQuestionInfo);
    }

    /**
     * 加入错题
     */
    @RequestMapping("/addWrongQuestion")
    @GlobalInterceptor
    public Result addWrongQuestion(@RequestHeader(value = "token") String token,
                                   @NotNull Integer questionId,
                                   String userAnswer) {
        String userId = redisComponent.getUserToken(token).getUserId();
        wrongQuestionService.addWrongQuestion(userId, questionId, userAnswer);
        return Result.success(null);
    }

    /**
     * 移除错题
     */
    @RequestMapping("/removeWrongQuestion")
    @GlobalInterceptor
    public Result removeWrongQuestion(@RequestHeader(value = "token") String token,
                                      @NotNull Integer wrongId) {
        String userId = redisComponent.getUserToken(token).getUserId();
        wrongQuestionService.removeWrongQuestion(userId, wrongId);
        return Result.success(null);
    }

}
