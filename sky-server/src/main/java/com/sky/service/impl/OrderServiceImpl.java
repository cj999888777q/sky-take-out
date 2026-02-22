package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;


    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());

        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();

        List<ShoppingCart> list = shoppingCartMapper.get(shoppingCart);

        if(list == null || list.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        String address=addressBook.getProvinceName()+addressBook.getDistrictName()+addressBook.getDetail();

        //向订单表中插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(orders.UN_PAID);
        orders.setStatus(orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setPhone(addressBook.getPhone());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(address);
        orderMapper.insert(orders);


        List<OrderDetail> orderDetailList=new ArrayList<>();
        for(ShoppingCart cart:list){

            OrderDetail orderDetail = new OrderDetail();

            BeanUtils.copyProperties(cart,orderDetail);

            orderDetail.setOrderId(orders.getId());

            orderDetailList.add(orderDetail);

        }

        orderDetailMapper.insertBatch(orderDetailList);


        shoppingCartMapper.delete(shoppingCart);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();

    }

    @Override
    @Transactional
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO)
    {
        Long userId = BaseContext.getCurrentId();

        ordersPageQueryDTO.setUserId(userId);

        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.historyOrders(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();

        if(page != null && page.getTotal() > 0){
            for(Orders orders:page.getResult())
            {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                List<OrderDetail> list = orderDetailMapper.getByOrdersId(orders.getId());
                orderVO.setOrderDetailList(list);
                orderVOList.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(),orderVOList);

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject=new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    @Override
    public void paySuccess(String orderNumber) {
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(orderNumber);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        Map map = new HashMap();

        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:"+orderNumber);

        String message = JSONObject.toJSONString(map);

        webSocketServer.sendToAllClient(message);
    }

    @Override
    public OrderVO orderDetail(Long id) {

        OrderVO orderVO = new OrderVO();

        Orders orders = orderMapper.getById(id);

        BeanUtils.copyProperties(orders,orderVO);

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrdersId(orders.getId());

        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;

    }

    @Override
    public void cancel(Long id) {

        Orders orders = orderMapper.getById(id);

        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if(orders.getStatus()>Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders ordersDB = new Orders();

        BeanUtils.copyProperties(orders,ordersDB);

        if(orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            ordersDB.setPayStatus(Orders.REFUND);
        }

        ordersDB.setCancelReason("用户退款");
        ordersDB.setCancelTime(LocalDateTime.now());
        ordersDB.setStatus(Orders.CANCELLED);

        orderMapper.update(ordersDB);

    }

    @Override
    public void repetition(Long id) {

        Orders orders = orderMapper.getById(id);

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrdersId(orders.getId());

        List<ShoppingCart> list = new ArrayList<>();

        for(OrderDetail orderDetail:orderDetailList)
        {
            ShoppingCart shoppingCart = new ShoppingCart();

            BeanUtils.copyProperties(orderDetail,shoppingCart,"id");

            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setUserId(BaseContext.getCurrentId());

            list.add(shoppingCart);

        }

        shoppingCartMapper.insertBach(list);

    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {

        Page<Orders> page = orderMapper.historyOrders(ordersPageQueryDTO);

        List<Orders> orders = page.getResult();

        List<OrderVO> orderVOList = new ArrayList<>();

        for(Orders order:orders){
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order,orderVO);

            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrdersId(order.getId());

            String orderDishes = abc(orderDetailList);

            orderVO.setOrderDishes(orderDishes);
            orderVO.setOrderDetailList(orderDetailList);

            orderVOList.add(orderVO);
        }

        return new PageResult(page.getTotal(),orderVOList);
    }

    private String abc(List<OrderDetail> orderDetailList)
    {
        StringBuilder num = new StringBuilder();

        for(OrderDetail orderDetail:orderDetailList)
        {
            num.append(orderDetail.getName()).append("*").append(orderDetail.getNumber()).append(" ");
        }

        return num.toString();

    }


    @Override
    public OrderStatisticsVO statistics() {

         Integer toBeConfirmed =  orderMapper.count(Orders.TO_BE_CONFIRMED);
         Integer confirmed = orderMapper.count(Orders.CONFIRMED);
         Integer deliveryInProgress = orderMapper.count(Orders.DELIVERY_IN_PROGRESS);

         OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
         orderStatisticsVO.setConfirmed(confirmed);
         orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
         orderStatisticsVO.setToBeConfirmed(toBeConfirmed);

         return orderStatisticsVO;


    }

    @Override
    public OrderVO details(Long id) {

        Orders orders = orderMapper.getById(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrdersId(orders.getId());
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {

        Orders orders = orderMapper.getById(ordersConfirmDTO.getId());

        if(!Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);

    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {

        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());

        if(!Objects.equals(orders.getStatus(), Orders.TO_BE_CONFIRMED))
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelReason("已拒绝");
        orders.setPayStatus(Orders.CONFIRMED);
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());

        if(!Objects.equals(orders.getStatus(), Orders.CONFIRMED))
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setPayStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {

        Orders orders = orderMapper.getById(id);

        if(!Objects.equals(orders.getStatus(), Orders.CONFIRMED))
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);

        if(!Objects.equals(orders.getStatus(), Orders.DELIVERY_IN_PROGRESS))
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);

        if(orders==null)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map = new HashMap();

        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单:"+orders.getNumber());

        String s = JSONObject.toJSONString(map);

        webSocketServer.sendToAllClient(s);

    }


}
