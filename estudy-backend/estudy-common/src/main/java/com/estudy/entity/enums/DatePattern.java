package com.estudy.entity.enums;

import lombok.Getter;

/**
 * 日期格式枚举
 */
@Getter
public enum DatePattern {
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),
    YYYY_MM_DD("yyyy-MM-dd"),
    YYYY_MM("yyyy-MM"),
    YYYYMMDD("yyyyMMdd"),
    YYYYMM("yyyyMM");

    private final String pattern;

    DatePattern(String pattern) {
        this.pattern = pattern;
    }
}
