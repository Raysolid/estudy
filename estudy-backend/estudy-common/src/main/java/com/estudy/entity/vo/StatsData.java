package com.estudy.entity.vo;

import lombok.Data;

@Data
public class StatsData {

    private Integer paperCount;             // 试卷数量
    private Integer continuousSignCount;    // 连续签到天数
    private Integer totalExams;             // 总考试数

}
