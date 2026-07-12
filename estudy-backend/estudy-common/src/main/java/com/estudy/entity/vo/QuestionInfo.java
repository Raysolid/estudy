package com.estudy.entity.vo;

import lombok.Data;

@Data
public class QuestionInfo {

    private Integer categoryId;
    private Integer questionId;
    private Integer type;
    private String content;
    private Integer difficulty;
    private String analysis;
    private Integer answerCount;    // 答题人数
    private Double correctRate;     // 正确率

}
