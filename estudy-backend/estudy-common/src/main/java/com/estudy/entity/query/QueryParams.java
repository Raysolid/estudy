package com.estudy.entity.query;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用查询参数
 */
@Data
public class QueryParams extends BaseParam {

    /**
     * 动态查询条件
     */
    private Map<String, Object> conditions = new HashMap<>();

    /**
     * 模糊查询条件
     */
    private Map<String, String> fuzzyConditions = new HashMap<>();

    /**
     * 时间条件
     */
    private String timeField;
    private String startTime;
    private String endTime;

    public void setConditions(String field, Object value) {
        this.conditions.put(field, value);
    }

    public void setFuzzyConditions(String field, String fuzzy) {
        this.fuzzyConditions.put(field, fuzzy);
    }

}
