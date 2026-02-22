package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "店铺相关")
@RequestMapping("/admin/shop")
@Slf4j
public class ShopController {


    @Autowired
    private RedisTemplate redisTemplate;


    @ApiOperation("设置店铺状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {

        log.info("设置店铺状态:{}",status);

        redisTemplate.opsForValue().set("shop_status", status);

        return Result.success();
    }

    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus()
    {

        Integer status = (Integer) redisTemplate.opsForValue().get("shop_status");

        log.info("获取店铺状态:{}",status);

        return Result.success(status);
    }




}
