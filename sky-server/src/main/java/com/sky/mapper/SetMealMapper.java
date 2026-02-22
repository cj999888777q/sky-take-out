package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealMapper {

    @Select("select count(*) from setmeal where category_id=#{id}")
    Integer getByCategoryId(Long id);

    @Options(useGeneratedKeys = true,keyProperty = "id")
    @AutoFill(OperationType.INSERT)
    @Insert("insert into setmeal(category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) VALUES " +
            "(#{categoryId},#{name},#{price},#{status},#{description},#{image},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void save(Setmeal setmeal);

    Page<SetmealVO> page(SetmealPageQueryDTO setmealPageQueryDTO);

    List<Setmeal> getByIds(List<Long> ids);

    void deleteByIds(List<Long> ids);

    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    List<Setmeal> list(Setmeal setmeal);

    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);

    @Select("select count(*) from setmeal where status=#{i}")
    Integer getSumByStatus(int i);
}
