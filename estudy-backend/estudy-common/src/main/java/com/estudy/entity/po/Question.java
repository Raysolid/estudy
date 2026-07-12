package com.estudy.entity.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class Question implements Serializable {

    /**
     * 题目id
     */
    private Integer questionId;

    /**
     * 所属试卷id
     */
    private Integer paperId;

    /**
     * 题目类型
     */
    private Integer type;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 1简单 2中等 3困难
     */
    private Integer difficulty;

    /**
     * 选项
     */
    private String options;

    /**
     * 答案
     */
    private String answer;

    /**
     * 题目解析
     */
    private String analysis;

    /**
     * 是否解锁题目
     */
    private Boolean unLock;

}
