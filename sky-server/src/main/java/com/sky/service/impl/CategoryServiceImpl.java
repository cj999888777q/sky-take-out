package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class CategoryServiceImpl implements CategoryService {


    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetMealMapper setMealMapper;


    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());

        Page<Category> page = categoryMapper.page(categoryPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public List<Category> getByList(Integer type) {
           return categoryMapper.getByList(type);
    }

    @Override
    public void deleteById(Long id) {

           List<Dish> list = dishMapper.getByCategoryId(id);

           if(!list.isEmpty()){
               throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
           }

           Integer num = setMealMapper.getByCategoryId(id);

           if(num>0)
           {
               throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
           }

           categoryMapper.deleteById(id);
    }

    @Override
    public void save(CategoryDTO categoryDTO) {

        Category category = new Category();

        BeanUtils.copyProperties(categoryDTO,category);

        category.setStatus(0);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.save(category);
    }

    @Override
    public void upOrDown(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();

        categoryMapper.update(category);
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
           Category category = new Category();
           BeanUtils.copyProperties(categoryDTO,category);

           category.setUpdateTime(LocalDateTime.now());
           category.setUpdateUser(BaseContext.getCurrentId());

           categoryMapper.update(category);

    }

    @Override
    public List<Category> getByType(Integer type) {

        return categoryMapper.getByType(type);
    }


}
