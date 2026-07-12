package com.estudy.entity.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserExamDto {

    private Integer paperId;
    private Date startTime;
    private List<String> userAnswers;

}
