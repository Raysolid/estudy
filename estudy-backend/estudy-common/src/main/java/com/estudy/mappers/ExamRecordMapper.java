package com.estudy.mappers;

import com.estudy.entity.query.QueryParams;
import com.estudy.entity.query.SimplePage;
import com.estudy.entity.vo.ExamReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 *  数据库操作接口
 */
public interface ExamRecordMapper<T,P> extends BaseMapper<T,P> {

    /**
     * 根据参数查询记录
     */
    List<ExamReport> selectRecordsByParams(@Param("query") QueryParams params, @Param("page") SimplePage page);

    /**
     * 根据id查询记录
     */
    ExamReport selectRecordById(@Param("recordId") Integer recordId);

    /**
     * 统计数量
     */
    Map<String, Object> selectStatsCount();

}
