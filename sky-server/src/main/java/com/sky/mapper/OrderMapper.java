package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {


    void insert(Orders orders);

    Page<Orders> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    void update(Orders orders);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select count(*) from orders where status = #{status}")
    Integer count(Integer status);


    @Select("select * from orders where status = #{status} and order_time <= #{time}")
    List<Orders> getByStatusAndTime(Integer status, LocalDateTime time);

    Double turnoverStatistics(Map map);

    Integer ordersStatistics(Map map);


    List<GoodsSalesDTO> top10(Map map);
}
