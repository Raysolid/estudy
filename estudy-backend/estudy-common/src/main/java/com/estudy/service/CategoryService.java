package com.estudy.service;

import com.estudy.entity.po.Category;

import java.util.List;

public interface CategoryService {

    /**
     * 获取分类列表
     */
    List<Category> getCategoryList(String keyword);

    /**
     * 添加分类
     */
    void addCategory(String name, String description);

    /**
     * 修改分类
     */
    void updateCategory(Integer categoryId, String name, String description);

    /**
     * 删除分类
     */
    void deleteCategory(Integer categoryId);
}
