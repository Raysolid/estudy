package com.estudy.admin.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.entity.po.Question;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.entity.vo.Result;
import com.estudy.service.QuestionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class QuestionController {

    @Resource
    private QuestionService questionService;

    /**
     * 获取题目列表
     */
    @RequestMapping("/getQuestionList")
    @GlobalInterceptor(adminLogin = true)
    public Result getQuestionList(String content, Integer type, Integer difficulty,
                               @NotNull Integer pageNo, @NotNull Integer pageSize) {
        PaginationResult<Question> result = questionService.getQuestionList(content, type, difficulty, pageNo, pageSize);
        return Result.success(result);
    }

    /**
     * 新增/修改题目
     */
    @RequestMapping("/editQuestion")
    @GlobalInterceptor(adminLogin = true)
    public Result editQuestion(@NotNull Question question) {
        Boolean isAdd = question.getQuestionId() == null; // 是否为添加
        questionService.editQuestion(question, isAdd);
        return Result.success(null);
    }

    /**
     * 删除题目
     */
    @RequestMapping("/deleteQuestion")
    @GlobalInterceptor(adminLogin = true)
    public Result deleteQuestion(@NotNull Integer questionId) {
        questionService.deleteQuestion(questionId);
        return Result.success(null);
    }

    /**
     * 获取未组卷的题目
     */
    @RequestMapping("/getNoComposeQuestions")
    @GlobalInterceptor(adminLogin = true)
    public Result getNoComposeQuestions(@NotNull Integer pageNo, @NotNull Integer pageSize) {
        PaginationResult<Question> result = questionService.getNoComposeQuestions(pageNo, pageSize);
        return Result.success(result);
    }

    /**
     * 根据试卷获取题目列表
     */
    @RequestMapping("/getQuestionsByPaper")
    @GlobalInterceptor(adminLogin = true)
    public Result getQuestionsByPaper(@NotNull Integer paperId) {
        List<Question> result = questionService.getQuestionsByPaper(paperId);
        return Result.success(result);
    }

}
