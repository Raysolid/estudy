package com.estudy.web.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.entity.po.Category;
import com.estudy.entity.po.Paper;
import com.estudy.entity.vo.QuestionInfo;
import com.estudy.entity.vo.PaginationResult;
import com.estudy.entity.vo.Result;
import com.estudy.service.CategoryService;
import com.estudy.service.PaperService;
import com.estudy.service.QuestionService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@Validated
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private PaperService paperService;

    @Resource
    private QuestionService questionService;

    /**
     * 获取分类列表
     */
    @RequestMapping("/getList")
    public Result getList() {
        List<Category> categoryList = categoryService.getCategoryList(null);
        return Result.success(categoryList);
    }

    /**
     * 获取分类下的热门试卷
     */
    @RequestMapping("/getHotPapers")
    public Result getHotPapers(@NotNull Integer categoryId) {
        List<Paper> hotList = paperService.getHotPapersByCategory(categoryId);
        return Result.success(hotList);
    }

    @RequestMapping("/getHotQuestions")
    public Result getHotQuestions(@NotNull Integer categoryId) {
        List<QuestionInfo> result = questionService.getHotQuestions(categoryId);
        return Result.success(result);
    }

    /**
     * 获取分类下的试卷
     */
    @RequestMapping("/getPapers")
    @GlobalInterceptor
    public Result getPapers(@NotNull Integer categoryId, Integer pageNo) {
        PaginationResult<Paper> result = paperService.getPapersByCategory(categoryId, pageNo);
        return Result.success(result);
    }

}
