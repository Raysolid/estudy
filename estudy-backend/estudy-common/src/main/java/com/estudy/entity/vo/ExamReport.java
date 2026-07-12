package com.estudy.entity.vo;

import com.estudy.entity.po.AnswerDetail;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ExamReport {

    private Integer recordId;   // 考试记录id
    private String userId;      // 用户id
    private Integer paperId;    // 试卷id
    private Integer categoryId; // 所属分类id
    private Integer score;      // 得分
    private Integer duration;   // 用时
    private Date startTime;     // 考试时间
    private String name;        // 试卷名称
    private String description; // 试卷描述
    private Integer questionCount; // 题目数量
    private Integer totalScore; // 总分
    private String tag;         // 标签
    private List<AnswerDetail> answerDetails;   // 作答详情

}
