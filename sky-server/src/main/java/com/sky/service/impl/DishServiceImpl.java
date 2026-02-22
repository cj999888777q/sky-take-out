package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;


    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
           Dish dish=new Dish();

        BeanUtils.copyProperties(dishDTO,dish);
        dish.setStatus(0);

        dishMapper.save(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();

        if(flavors!=null&& !flavors.isEmpty()){
            for(DishFlavor df:flavors){
                df.setDishId(dish.getId());
            }
            dishFlavorMapper.saveDishFlavors(flavors);
        }
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.page(dishPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());

    }


    @Override
    @Transactional
    public void delete(List<Long> ids) {

        List<Dish> list = dishMapper.getByIds(ids);

        for (Dish dish : list) {
            if(dish.getStatus()==1){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        List<SetmealDish> listSetMealDish = setMealDishMapper.getByDishIds(ids);

        if (listSetMealDish != null && !listSetMealDish.isEmpty()) {

            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        dishMapper.deleteByIds(ids);

        dishFlavorMapper.deleteByDishIds(ids);

    }

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {

        Dish dish=new Dish();

        BeanUtils.copyProperties(dishDTO,dish);

        dishMapper.update(dish);

        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();

        if(flavors!=null&& !flavors.isEmpty()){
            for(DishFlavor df:flavors){
                df.setDishId(dish.getId());
            }
            dishFlavorMapper.saveDishFlavors(flavors);
        }

    }

    @Override
    @Transactional
    public DishVO getById(Long id) {

        DishVO dishVO = new DishVO();

        Dish dish = dishMapper.getById(id);

        BeanUtils.copyProperties(dish,dishVO);

        List<DishFlavor> list = dishFlavorMapper.getByDishId(id);

        dishVO.setFlavors(list);

        return dishVO;
    }

    @Override
    public void startOrStop(Integer status, Long id) {

        Dish dish = new Dish();

        dish.setStatus(status);
        dish.setId(id);

        dishMapper.update(dish);

    }

    @Override
    public List<Dish> getList(Long categoryId) {

        return dishMapper.getByCategoryId(categoryId);

    }

    @Override
    public List<DishVO> userGetByCategoryId(Long categoryId) {

        List<DishVO> dishVOList=new ArrayList<>();

        List<Dish> list = dishMapper.getUpByCategoryId(categoryId);

        for(Dish d:list){

            DishVO dishVO=new DishVO();

            BeanUtils.copyProperties(d,dishVO);

            List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(dishFlavors);

            dishVOList.add(dishVO);
        }

        return dishVOList;

    }


}
