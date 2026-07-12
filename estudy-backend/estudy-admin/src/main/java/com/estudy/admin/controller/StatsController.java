package com.estudy.admin.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.entity.vo.Result;
import com.estudy.service.StatsService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Validated
public class StatsController {

    @Resource
    private StatsService statsService;

    /**
     * 获取概览数据
     */
    @RequestMapping("/getOverviewData")
    @GlobalInterceptor(adminLogin = true)
    public Result getOverviewData() {
        Map<String, Integer> result = statsService.getOverviewData();
        return Result.success(result);
    }

    /**
     * 获取一周数据
     */
    @RequestMapping("/getWeekExamData")
    @GlobalInterceptor(adminLogin = true)
    public Result getWeekExamData() {
        Map<String, Long> result = statsService.getWeekExamData();
        return Result.success(result);
    }

    /**
     * 获取统计数据
     */
    @RequestMapping("/getStatsData")
    @GlobalInterceptor(adminLogin = true)
    public Result getStatsData() {
        Map<String, Object> result = statsService.getStatsData();
        return Result.success(result);
    }

}
