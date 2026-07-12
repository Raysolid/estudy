package com.estudy.entity.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class Category implements Serializable {

    /**
     * 分类id
     */
    private Integer categoryId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 试卷数量
     */
    private Integer count;

}
