package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private DishMapper dishMapper;


    @Override
    @Transactional
    public void add(ShoppingCartDTO shoppingCartDTO) {
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();

        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        shoppingCart.setDishId(dishId);
        shoppingCart.setSetmealId(setmealId);

        List<ShoppingCart> list = shoppingCartMapper.get(shoppingCart);

        if(list!=null&& !list.isEmpty()){
            shoppingCart = list.get(0);
            Integer number = shoppingCart.getNumber();
            shoppingCart.setNumber(++number);
            shoppingCartMapper.updateById(shoppingCart);
        }
        else
        {
            if(setmealId==null)
            {
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
            }
            else
            {
                List<Long> listNew = new ArrayList<>();
                listNew.add(setmealId);
                List<Setmeal> setmealList = setMealMapper.getByIds(listNew);

                Setmeal setmeal = setmealList.get(0);

                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());

            }

            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.add(shoppingCart);

        }




    }

    @Override
    public List<ShoppingCart> get()
    {
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        return shoppingCartMapper.get(shoppingCart);

    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        shoppingCart.setDishId(shoppingCartDTO.getDishId());
        shoppingCart.setSetmealId(shoppingCartDTO.getSetmealId());

        List<ShoppingCart> list = shoppingCartMapper.get(shoppingCart);

        shoppingCart = list.get(0);

        Integer number = shoppingCart.getNumber();

        if(number >1)
        {
            shoppingCart.setNumber(--number);
            shoppingCartMapper.updateById(shoppingCart);
        }
        else
        {
            shoppingCartMapper.delete(shoppingCart);
        }
    }

    @Override
    public void clean()
    {
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        shoppingCartMapper.delete(shoppingCart);

    }


}
