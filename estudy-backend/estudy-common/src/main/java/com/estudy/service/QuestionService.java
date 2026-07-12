package com.estudy.service;

import com.estudy.entity.po.Question;
import com.estudy.entity.vo.QuestionInfo;
import com.estudy.entity.vo.PaginationResult;

import java.util.List;

public interface QuestionService {

    /**
     * 获取分类下热门题目
     */
    List<QuestionInfo> getHotQuestions(Integer categoryId);

    /**
     * 搜索题目
     */
    PaginationResult<QuestionInfo> searchQuestions(String keyword, Integer categoryId, Integer difficulty, String sort, String sortOrder, Integer pageNo);

    /**
     * 获取题目列表
     */
    PaginationResult<Question> getQuestionList(String content, Integer type, Integer difficulty, Integer pageNo, Integer pageSize);

    /**
     * 获取题目信息
     */
    Question getQuestionInfo(String userId, Integer questionId);

    /**
     * 添加题目
     */
    void editQuestion(Question question, Boolean isAdd);

    /**
     * 删除题目
     */
    void deleteQuestion(Integer questionId);

    /**
     * 获取未组卷的题目
     */
    PaginationResult<Question> getNoComposeQuestions(Integer pageNo, Integer pageSize);

    /**
     * 根据试卷id获取题目id列表
     */
    List<Question> getQuestionsByPaper(Integer paperId);

}
