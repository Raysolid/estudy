package com.estudy.entity.po;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

@Data
public class ExamRecord implements Serializable {

    /**
     * 考试记录id
     */
    private Integer recordId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 试卷id
     */
    private Integer paperId;

    /**
     * 得分
     */
    private Integer score;

    /**
     * 考试用时
     */
    private Long duration;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

}
