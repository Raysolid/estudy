package com.estudy.mappers;

import com.estudy.entity.po.Paper;
import com.estudy.entity.query.SimplePage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface PaperMapper<T,P> extends BaseMapper<T,P> {

    /**
     * 根据分类id查询试卷
     */
    List<Paper> selectPaperListByCategoryId(@Param("categoryId") Integer categoryId, @Param("page") SimplePage page);

    /**
     * 获取多个分类下的试卷总数
     */
    List<Integer> selectPaperIdByCategoryIds(@Param("ids") Object[] ids);

    /**
     * 根据id数组查询多套试卷
     */
    List<Paper> selectPaperByIds(@Param("ids") List<Integer> ids);
}
