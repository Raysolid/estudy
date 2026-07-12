package com.estudy.service;

import com.estudy.entity.vo.PaginationResult;
import com.estudy.entity.vo.WrongQuestionInfo;

import java.util.List;

public interface WrongQuestionService {

    /**
     * 最近错题
     */
    List<WrongQuestionInfo> getRecentWrongQuestions(String userId);

    /**
     * 获取用户错题
     */
    PaginationResult<WrongQuestionInfo> getWrongQuestions(String userId, Integer pageNo);

    /**
     * 根据id获取错题信息
     */
    WrongQuestionInfo getWrongQuestionById(Integer questionId);

    /**
     * 加入错题
     */
    void addWrongQuestion(String userId, Integer questionId, String userAnswer);

    /**
     * 移除错题
     */
    void removeWrongQuestion(String userId, Integer wrongId);

}
