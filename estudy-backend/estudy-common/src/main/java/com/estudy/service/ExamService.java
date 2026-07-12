package com.estudy.service;

import com.estudy.entity.vo.ExamData;
import com.estudy.entity.vo.ExamReport;
import com.estudy.entity.vo.PaginationResult;

import java.util.Date;
import java.util.List;

public interface ExamService {

    /**
     * 获取近期考试记录
     */
    List<ExamReport> getRecentExam(String userId);

    /**
     * 获取历史考试记录
     */
    PaginationResult<ExamReport> getExamRecord(String userId, Integer pageNo);

    /**
     * 获取考试记录详情
     */
    ExamReport getRecordInfo(Integer recordId);

    /**
     * 获取试卷题目
     */
    ExamData getExamQuestions(Integer paperId, Boolean isExam);

    /**
     * 提交考试
     */
    Integer submitExam(Integer paperId, String userId, Date startTime, Date endTime, List<String> userAnswers);

    /**
     * 管理端获取考试记录
     */
    PaginationResult<ExamReport> getExamRecords(String userId, Integer paperId, Integer pageNo, Integer pageSize);
}
