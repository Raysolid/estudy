package com.estudy.entity.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class AnswerDetail implements Serializable {

    /**
     * 答题详情id
     */
    private Integer detailId;

    /**
     * 考试记录id
     */
    private Integer recordId;

    /**
     * 题目id
     */
    private Integer questionId;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * 0错误 1正确
     */
    private Integer isCorrect;

    /**
     * 题目信息
     */
    private String content;
    private String options;
    private String answer;
    private Boolean inWrongList;    // 是否已加入错题本

}
