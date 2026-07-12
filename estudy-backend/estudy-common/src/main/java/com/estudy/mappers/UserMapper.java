package com.estudy.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 用户信息表 数据库操作接口
 */
public interface UserMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据email查询
     */
    T selectByEmail(@Param("email") String email);

    /**
     * 更新用户积分
     */
    Integer updateUserPoints(@Param("userId") String userId, @Param("points") Integer points);

    /**
     * 统计用户积分情况
     */
    Map<String, Object> selectPointsCount();

}
