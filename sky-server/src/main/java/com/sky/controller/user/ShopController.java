package com.sky.controller.user;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "用户端店铺相关")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;


    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus()
    {
        Integer status = (Integer) redisTemplate.opsForValue().get("shop_status");

        log.info("店铺营业状态:{}",status);

        return Result.success(status);
    }

}
