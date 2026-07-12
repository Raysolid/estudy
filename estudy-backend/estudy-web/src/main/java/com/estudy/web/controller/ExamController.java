package com.estudy.web.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.component.RedisComponent;
import com.estudy.entity.dto.UserExamDto;
import com.estudy.entity.vo.ExamData;
import com.estudy.entity.vo.ExamReport;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.entity.vo.Result;
import com.estudy.service.ExamService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/exam")
@Validated
public class ExamController {

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ExamService examService;

    /**
     * 获取近期考试记录
     */
    @RequestMapping("/getRecentExam")
    @GlobalInterceptor
    public Result getRecentExam(@RequestHeader(value = "token") String token) {
        String userId = redisComponent.getUserToken(token).getUserId();
        List<ExamReport> result = examService.getRecentExam(userId);
        return Result.success(result);
    }

    /**
     * 获取历史考试记录
     */
    @RequestMapping("/getExamRecord")
    @GlobalInterceptor
    public Result getExamRecord(@RequestHeader(value = "token") String token,
                                Integer pageNo) {
        String userId = redisComponent.getUserToken(token).getUserId();
        PaginationResult<ExamReport> result = examService.getExamRecord(userId, pageNo);
        return Result.success(result);
    }

    /**
     * 获取记录详情
     */
    @RequestMapping("/getRecordDetail")
    @GlobalInterceptor
    public Result getRecordDetail(@NotNull Integer recordId) {
        ExamReport examReport = examService.getRecordInfo(recordId);
        return Result.success(examReport);
    }

    /**
     * 获取试卷题目
     */
    @RequestMapping("/getExamQuestions")
    @GlobalInterceptor
    public Result getExamQuestions(@NotNull Integer paperId,
                                   @NotNull Boolean isExam) {
        ExamData result = examService.getExamQuestions(paperId, isExam);
        return Result.success(result);
    }

    /**
     * 提交考试
     */
    @RequestMapping("/submitExam")
    @GlobalInterceptor
    public Result submitExam(@RequestHeader(value = "token") String token,
                             @NotNull UserExamDto userExamDto) {
        Date endTime = new Date();
        String userId = redisComponent.getUserToken(token).getUserId();
        Integer recordId = examService.submitExam(userExamDto.getPaperId(), userId,
                userExamDto.getStartTime(), endTime, userExamDto.getUserAnswers());
        return Result.success(recordId);
    }
}
