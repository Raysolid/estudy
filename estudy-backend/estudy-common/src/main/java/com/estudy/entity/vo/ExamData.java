package com.estudy.entity.vo;

import com.estudy.entity.po.Paper;
import com.estudy.entity.po.Question;
import lombok.Data;

import java.util.List;

@Data
public class ExamData {

    private Paper examInfo;
    private List<Question> questions;

}
