package com.estudy.mappers;

import com.estudy.entity.vo.QuestionInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface QuestionMapper<T,P> extends BaseMapper<T,P> {

    /**
     * 根据分类id查询热门题目
     */
    List<QuestionInfo> selectQuestionsByParams(@Param("query") P p);

    /**
     * 查询没有所属试卷的题目
     */
    List<T> selectListNoPaperId(@Param("start") Integer start, @Param("end") Integer pageSize);

    /**
     * 查询没有所属试卷的题目数量
     */
    Integer selectNoPaperIdCount();

    /**
     * 根据paperId批量修改
     */
    Integer updatePaperIdBatch(@Param("ids") Integer[] ids, @Param("paperId") Integer paperId);

}
