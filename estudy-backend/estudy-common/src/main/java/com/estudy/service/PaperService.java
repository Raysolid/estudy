package com.estudy.service;

import com.estudy.entity.po.Paper;
import com.estudy.entity.vo.PaginationResult;

import java.util.List;

public interface PaperService {

    /**
     * 根据用户偏好推荐试卷
     */
    List<Paper> recommendPapers(String userId);

    /**
     * 根据分类获取热门试卷
     */
    List<Paper> getHotPapersByCategory(Integer categoryId);

    /**
     * 根据分类获取试卷
     */
    PaginationResult<Paper> getPapersByCategory(Integer categoryId, Integer pageNo);

    /**
     * 解锁试卷
     */
    void unlockPaper(String userId, Integer paperId);

    /**
     * 获取试卷信息
     */
    Paper getPaperInfo(Integer paperId);

    /**
     * 获取试卷列表
     */
    PaginationResult<Paper> getPaperList(String name, Integer categoryId, String tag, Integer pageNo, Integer pageSize);

    /**
     * 新增试卷
     */
    void addPaper(Paper paper);

    /**
     * 修改试卷
     */
    void updatePaper(Paper paper);

    /**
     * 删除试卷
     */
    void deletePaper(Integer paperId);

    /**
     * 组卷
     */
    Paper composePaper(Integer paperId, Integer[] addIds, Integer[] removeIds);

}
