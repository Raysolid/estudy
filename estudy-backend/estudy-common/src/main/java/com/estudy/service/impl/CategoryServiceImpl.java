package com.estudy.service.impl;

import com.estudy.entity.enums.ResultCode;
import com.estudy.entity.po.Category;
import com.estudy.entity.po.Paper;
import com.estudy.entity.query.QueryParams;
import com.estudy.exception.BusinessException;
import com.estudy.mappers.CategoryMapper;
import com.estudy.mappers.PaperMapper;
import com.estudy.service.CategoryService;
import com.estudy.utils.StrUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper<Category, QueryParams> categoryMapper;

    @Resource
    private PaperMapper<Paper, QueryParams> paperMapper;

    @Override
    public List<Category> getCategoryList(String keyword) {
        QueryParams params = new QueryParams();
        if (!StrUtils.isEmpty(keyword)) {
            params.setFuzzyConditions("name", keyword);
        }
        return categoryMapper.selectList(params);
    }

    @Override
    public void addCategory(String name, String description) {
        if (StrUtils.isEmpty(name)) {
            throw new BusinessException("分类名称不能为空");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setCount(0);
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Integer categoryId, String name, String description) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        if (StrUtils.isEmpty(name)) {
            throw new BusinessException("分类名称不能为空");
        }
        category.setName(name);
        category.setDescription(description);
        categoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Integer categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException(ResultCode.CODE_400);
        }
        QueryParams params = new QueryParams();
        params.setConditions("category_id", categoryId);
        Integer count = paperMapper.selectCount(params);
        if (count > 0) {
            throw new BusinessException("该分类下还有试卷，无法进行删除");
        }
        categoryMapper.deleteById(categoryId);
    }

}
