package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetMealServiceImpl implements SetMealService {


    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private DishMapper dishMapper;



    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {

        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO, setmeal);

        setMealMapper.save(setmeal);

        List<SetmealDish> setMealDishes = setmealDTO.getSetmealDishes();

        for (SetmealDish setMealDish : setMealDishes) {

            setMealDish.setSetmealId(setmeal.getId());
        }

        setMealDishMapper.save(setMealDishes);




    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setMealMapper.page(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());

    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {

        List<Setmeal> list = setMealMapper.getByIds(ids);

        for (Setmeal setmeal : list) {
            if (setmeal.getStatus()==1)
            {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        setMealMapper.deleteByIds(ids);

        setMealDishMapper.deleteBySetMealIds(ids);


    }

    @Override
    @Transactional
    public SetmealVO getById(Long id) {

        List<Long> ids = new ArrayList<>();
        ids.add(id);
        List<Setmeal> setmealList = setMealMapper.getByIds(ids);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmealList.get(0), setmealVO);

        List<SetmealDish> list = setMealDishMapper.getBySetMealId(id);

        setmealVO.setSetmealDishes(list);

        return setmealVO;

    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {

        Setmeal setmeal = new Setmeal();

        BeanUtils.copyProperties(setmealDTO, setmeal);

        setMealMapper.update(setmeal);

        List<Long> list = new ArrayList<>();
        list.add(setmeal.getId());
        setMealDishMapper.deleteBySetMealIds(list);

        List<SetmealDish> setMealDishes = setmealDTO.getSetmealDishes();

        for (SetmealDish setMealDish : setMealDishes) {
            setMealDish.setSetmealId(setmeal.getId());
        }

        setMealDishMapper.save(setMealDishes);
    }

    @Override
    public void startOrDown(Integer status, Long id) {

        if (status == 1) {
            List<SetmealDish> setMealDishes = setMealDishMapper.getBySetMealId(id);
            List<Long> ids = new ArrayList<>();
            for (SetmealDish setMealDish : setMealDishes) {
                ids.add(setMealDish.getDishId());
            }

            List<Dish> dishList = dishMapper.getByIds(ids);

            for (Dish dish : dishList) {
                if(dish.getStatus()==0)
                {
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }

        }

        Setmeal setmeal = Setmeal.builder()
                .status(status)
                .id(id)
                .build();

        setMealMapper.update(setmeal);
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        return setMealMapper.list(setmeal);

    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setMealMapper.getDishItemBySetmealId(id);

    }


}
