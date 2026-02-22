package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> getByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void save(Dish dish);

    Page<DishVO> page(DishPageQueryDTO dishPageQueryDTO);

    List<Dish> getByIds(List<Long> ids);

    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    @Select("select * from dish where status = 1 and category_id=#{categoryId}")
    List<Dish> getUpByCategoryId(Long categoryId);

    @Select("select count(*) from dish where status=#{i}")
    Integer getSumStatus(int i);
}
