package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {
    PageResult page(CategoryPageQueryDTO categoryPageQueryDTO);

    List<Category> getByList(Integer type);

    void deleteById(Long id);

    void save(CategoryDTO categoryDTO);

    void upOrDown(Integer status, Long id);

    void update(CategoryDTO categoryDTO);

    List<Category> getByType(Integer type);
}
