package com.estudy.service;

import com.estudy.entity.vo.StatsData;

import java.util.Map;

public interface StatsService {

    /**
     * 获取用户统计数据
     */
    StatsData getStatsData(String userId);

    /**
     * 管理端获取概览数据
     */
    Map<String, Integer> getOverviewData();

    /**
     * 管理端获取近一周考试人数
     */
    Map<String, Long> getWeekExamData();

    /**
     * 管理端获取统计数据
     */
    Map<String, Object> getStatsData();
}
