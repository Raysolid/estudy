package com.estudy.entity.vo;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WrongQuestionInfo {

    private Integer wrongId;
    private Integer questionId;
    private String name;    // 所属试卷名称
    private String content; // 试题内容
    private Integer type;   // 试题类型
    private Integer difficulty; // 难度
    private String options; // 试题选项
    private String analysis; // 试题解析
    private String answer;  // 正确答案
    private String userAnswer;  // 用户答案
    private LocalDate wrongTime; // 最后一次错误时间

}
