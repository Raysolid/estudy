package com.estudy.entity.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class Paper implements Serializable {

    /**
     * 试卷id
     */
    private Integer paperId;

    /**
     * 所属分类id
     */
    private Integer categoryId;

    /**
     * 试卷名称
     */
    private String name;

    /**
     * 试卷描述
     */
    private String description;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 答题时间(分钟)
     */
    private Integer duration;

    /**
     * 总分
     */
    private Integer totalScore;

    /**
     * 标签
     */
    private String tag;

    /**
     * 所需积分
     */
    private Integer pointsRequired;

    /**
     * 试卷难度（平均）
     */
    private Integer difficulty;

}
