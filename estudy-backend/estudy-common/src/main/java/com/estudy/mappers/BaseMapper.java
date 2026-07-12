package com.estudy.mappers;

import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.List;

interface BaseMapper<T, P> {

    /**
     * 根据主键id查询
     */
    T selectById(@Param("id") Serializable id);

    /**
     * 根据主键id更新
     */
    Integer updateById(@Param("bean") T t);

    /**
     * 根据主键id删除
     */
    Integer deleteById(@Param("id") Serializable id);

    /**
     * 根据参数查询集合
     */
    List<T> selectList(@Param("query") P p);

    /**
     * 根据参数查询数量
     */
    Integer selectCount(@Param("query") P p);

    /**
     * 插入数据
     */
    Integer insert(@Param("bean") T t);

    /**
     * 插入或者更新
     */
    Integer insertOrUpdate(@Param("bean") T t);

    /**
     * 批量插入
     */
    Integer insertBatch(@Param("list") List<T> list);

    /**
     * 批量插入或更新
     */
    Integer insertOrUpdateBatch(@Param("list") List<T> list);

    /**
     * 多条件更新
     */
    Integer updateByParam(@Param("bean") T t, @Param("query") P p);

    /**
     * 多条件删除
     */
    Integer deleteByParam(@Param("query") P p);
}
