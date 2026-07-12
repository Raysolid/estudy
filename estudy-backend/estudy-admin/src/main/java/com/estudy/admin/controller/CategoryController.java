package com.estudy.admin.controller;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.entity.po.Category;
import com.estudy.entity.vo.Result;
import com.estudy.service.CategoryService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @RequestMapping("/getCategoryList")
    @GlobalInterceptor(adminLogin = true)
    public Result getCategoryList(String keyword) {
        List<Category> categoryList = categoryService.getCategoryList(keyword);
        return Result.success(categoryList);
    }

    /**
     * 新增/修改分类
     */
    @RequestMapping("/editCategory")
    @GlobalInterceptor(adminLogin = true)
    public Result editCategory(Integer categoryId,
                               String name, String description) {
        if (categoryId == null) {
            categoryService.addCategory(name, description);
        } else {
            categoryService.updateCategory(categoryId, name, description);
        }
        return Result.success(null);
    }

    /**
     * 删除分类
     */
    @RequestMapping("/deleteCategory")
    @GlobalInterceptor(adminLogin = true)
    public Result deleteCategory(@NotNull Integer categoryId) {
        categoryService.deleteCategory(categoryId);
        return Result.success(null);
    }

}
