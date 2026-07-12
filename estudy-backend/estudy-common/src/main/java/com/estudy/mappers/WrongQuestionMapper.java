package com.estudy.mappers;

import com.estudy.entity.query.SimplePage;
import com.estudy.entity.vo.WrongQuestionInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据库操作接口
 */
public interface WrongQuestionMapper<T, P> extends BaseMapper<T, P> {

    /**
     * 根据用户id查询错题
     */
    List<WrongQuestionInfo> selectWrongQuestionList(@Param("userId") String userId, @Param("page") SimplePage page, @Param("showDetail") Boolean showDetail);

    /**
     * 根据题目id查询错题
     */
    WrongQuestionInfo selectWrongQuestionById(@Param("questionId") Integer questionId);

}
