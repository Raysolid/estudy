package com.estudy.admin.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.entity.po.Paper;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.entity.vo.Result;
import com.estudy.service.PaperService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class PaperController {

    @Resource
    private PaperService paperService;

    /**
     * 获取试卷列表
     */
    @RequestMapping("/getPaperList")
    @GlobalInterceptor(adminLogin = true)
    public Result getPaperList(String name, Integer categoryId, String tag,
                               @NotNull Integer pageNo, @NotNull Integer pageSize) {
        PaginationResult<Paper> result = paperService.getPaperList(name, categoryId, tag, pageNo, pageSize);
        return Result.success(result);
    }

    /**
     * 新增/修改试卷
     */
    @RequestMapping("/editPaper")
    @GlobalInterceptor(adminLogin = true)
    public Result editPaper(@NotNull Paper paper) {
        Integer paperId = paper.getPaperId();
        if (paperId == null) {
            paperService.addPaper(paper);
        } else {
            paperService.updatePaper(paper);
        }
        return Result.success(null);
    }

    /**
     * 删除试卷
     */
    @RequestMapping("/deletePaper")
    @GlobalInterceptor(adminLogin = true)
    public Result deletePaper(@NotNull Integer paperId) {
        paperService.deletePaper(paperId);
        return Result.success(null);
    }

    /**
     * 组卷
     */
    @RequestMapping("/composePaper")
    @GlobalInterceptor(adminLogin = true)
    public Result composePaper(@NotNull Integer paperId,
                               @NotNull Integer[] addIds,
                               @NotNull Integer[] removeIds) {
        Paper paper = paperService.composePaper(paperId, addIds, removeIds);
        return Result.success(paper);
    }

}
