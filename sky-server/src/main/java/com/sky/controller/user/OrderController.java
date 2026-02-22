package com.sky.controller.user;


import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户订单相关")
@Slf4j
public class OrderController {


    @Autowired
    private OrderService orderService;


    @ApiOperation("用户下单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO)
    {
        log.info("用户下单:{}", ordersSubmitDTO);

        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);

        return Result.success(orderSubmitVO);
    }

    @ApiOperation("用户查看历史订单")
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO)
    {
        log.info("用户查看历史订单:{}", ordersPageQueryDTO);

        PageResult pageResult = orderService.historyOrders(ordersPageQueryDTO);

        return Result.success(pageResult);

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
//        //todo 这里模拟了一个微信支付数据给前端
//        OrderPaymentVO orderPaymentVO = new OrderPaymentVO();
//       orderPaymentVO.setNonceStr("mockedNonceStr"); // 模拟随机字符串
//       orderPaymentVO.setPackageStr("prepay_id=mockedPrepayId"); // 微信支付需要 "prepay_id=" 前缀
//       orderPaymentVO.setSignType("MD5"); // 签名类型
//       orderPaymentVO.setTimeStamp(String.valueOf(System.currentTimeMillis() / 1000)); // 当前时间戳
//        orderPaymentVO.setPaySign("mockedPaySign"); // 模拟签名数据

//        log.info("模拟生成的预支付交易单：{}", orderPaymentVO);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success(orderPaymentVO);
    }

    @ApiOperation("查询订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id)
    {
        log.info("查询订单详情:{}",id);

        OrderVO orderVO = orderService.orderDetail(id);

        return Result.success(orderVO);
    }

    @ApiOperation("用户取消订单")
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id)
    {
        log.info("用户取消订单:{}",id);

        orderService.cancel(id);

        return Result.success();
    }

    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id)
    {
        log.info("再来一单:{}",id);

        orderService.repetition(id);

        return Result.success();
    }

    @ApiOperation("用户催单")
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id)
    {
        log.info("用户催单:{}",id);

        orderService.reminder(id);

        return Result.success();
    }




}
