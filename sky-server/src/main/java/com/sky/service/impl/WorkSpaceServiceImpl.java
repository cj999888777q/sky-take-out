package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;


@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {
    
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private SetMealMapper setMealMapper;
    @Autowired
    private DishMapper dishMapper;
    

    @Override
    public BusinessDataVO businessData(LocalDateTime begin,LocalDateTime end) {

        Map map = new HashMap();

        map.put("beginTime", begin);
        map.put("endTime", end);

        Integer num = userMapper.userStatistics(map);

        if (num ==null) {
            num=0;
        }

        Integer num1 = orderMapper.ordersStatistics(map);
        if (num1 == null) {
            num1=0;
        }

        map.put("status", Orders.COMPLETED);

        Integer num2 = orderMapper.ordersStatistics(map);
        if (num2 == null) {
            num2=0;
        }

        Double orderCompletionRate = 0.0;

        if(num2!=0)
        {
            orderCompletionRate = (double) num2/num1;
        }

        Double aDouble = orderMapper.turnoverStatistics(map);

        if (aDouble == null) {
            aDouble=0.0;
        }

        Double unitPrice = 0.0;

        if(aDouble!=0)
        {
            unitPrice = aDouble/num2;
        }

        return BusinessDataVO.builder()
                .turnover(aDouble)
                .validOrderCount(num2)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(num)
                .build();
    }


    @Override
    public OrderOverViewVO overviewOrders() {
        Map map=new HashMap();

        LocalDateTime beginTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        map.put("beginTime",beginTime);

        Integer allOrders=orderMapper.ordersStatistics(map);

        map.put("status",Orders.TO_BE_CONFIRMED);

        Integer waitingOrders=orderMapper.ordersStatistics(map);

        map.put("status",Orders.CONFIRMED);

        Integer deliveredOrders=orderMapper.ordersStatistics(map);

        map.put("status",Orders.COMPLETED);

        Integer completedOrders=orderMapper.ordersStatistics(map);

        map.put("status",Orders.CANCELLED);

        Integer cancelledOrders=orderMapper.ordersStatistics(map);

        return OrderOverViewVO.builder()
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders)
                .waitingOrders(waitingOrders)
                .allOrders(allOrders)
                .build();

    }

    @Override
    public SetmealOverViewVO overviewSetmeals() {
        Integer sum1=setMealMapper.getSumByStatus(1);
        Integer sum2=setMealMapper.getSumByStatus(0);

        return SetmealOverViewVO.builder()
                .sold(sum1)
                .discontinued(sum2)
                .build();
    }

    @Override
    public DishOverViewVO overviewDishes() {
        Integer sum1 = dishMapper.getSumStatus(1);
        Integer sum2 = dishMapper.getSumStatus(0);

        return DishOverViewVO.builder()
                .sold(sum1)
                .discontinued(sum2)
                .build();
    }
}
