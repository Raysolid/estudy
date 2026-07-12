package com.estudy.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Data
public class WrongQuestion implements Serializable {

    /**
     * 错题id
     */
    private Integer wrongId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 题目id
     */
    private Integer questionId;

    /**
     * 用户错误答案
     */
    private String userAnswer;

    /**
     * 最后一次错误时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date wrongTime;

}
