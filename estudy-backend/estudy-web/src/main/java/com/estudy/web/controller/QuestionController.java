package com.estudy.web.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.component.RedisComponent;
import com.estudy.entity.po.Question;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.entity.vo.QuestionInfo;
import com.estudy.entity.vo.Result;
import com.estudy.service.QuestionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/question")
@Validated
public class QuestionController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private QuestionService questionService;

    /**
     * 搜索
     */
    @RequestMapping("/search")
    @GlobalInterceptor
    public Result search(@NotEmpty String keyword,
                         Integer categoryId, Integer difficulty,
                         String sort, String sortOrder,
                         Integer pageNo) {
        PaginationResult<QuestionInfo> result = questionService.searchQuestions(keyword, categoryId, difficulty, sort, sortOrder, pageNo);
        return Result.success(result);
    }

    /**
     * 获取题目详情
     */
    @RequestMapping("/getQuestionDetail")
    @GlobalInterceptor
    public Result getQuestionDetail(@RequestHeader(value = "token") String token,
                                    @NotNull Integer questionId) {
        String userId = redisComponent.getUserToken(token).getUserId();
        Question question = questionService.getQuestionInfo(userId, questionId);
        return Result.success(question);
    }

}
