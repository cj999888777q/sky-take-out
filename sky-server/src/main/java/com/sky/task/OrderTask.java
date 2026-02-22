package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;


    /**
     * 定时处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void timeOutTask()
    {
        log.info("处理超时订单:{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now();

        time=time.plusMinutes(-15);

        List<Orders> list =orderMapper.getByStatusAndTime(Orders.PENDING_PAYMENT,time);

        if(list!=null&& !list.isEmpty())
        {
            for(Orders o:list)
            {
                o.setStatus(Orders.CANCELLED);
                o.setCancelReason("订单超时");
                o.setCancelTime(LocalDateTime.now());
                orderMapper.update(o);
            }
        }
    }


    /**
     * 定时处理派送中订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void delivery()
    {
        log.info("处理派送中订单:{}",LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> list =orderMapper.getByStatusAndTime(Orders.DELIVERY_IN_PROGRESS,time);

        if(list!=null&& !list.isEmpty())
        {
            for (Orders o:list)
            {
                o.setStatus(Orders.COMPLETED);
                orderMapper.update(o);
            }
        }
    }
}
