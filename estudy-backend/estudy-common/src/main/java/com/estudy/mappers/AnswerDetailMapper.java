package com.estudy.mappers;

import com.estudy.entity.po.AnswerDetail;

import java.util.List;

/**
 *  数据库操作接口
 */
public interface AnswerDetailMapper<T,P> extends BaseMapper<T,P> {

    /**
     * 查询作答详情
     */
    List<AnswerDetail> selectAnswerDetails(Integer recordId);

}
