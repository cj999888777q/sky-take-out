package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    List<SetmealDish> getByDishIds(List<Long> ids);

    void save(List<SetmealDish> setMealDishes);

    void deleteBySetMealIds(List<Long> ids);

    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> getBySetMealId(Long id);
}
