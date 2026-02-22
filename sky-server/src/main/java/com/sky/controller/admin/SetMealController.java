package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetMealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关")
@Slf4j
public class SetMealController {


    @Autowired
    private SetMealService setMealService;


    @ApiOperation("新增套餐")
    @PostMapping()
    @CacheEvict(cacheNames = "userSetMeal",key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO)
    {
        log.info("新增套餐:{}", setmealDTO);

        setMealService.save(setmealDTO);

        return Result.success();
    }

    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO)
    {
        log.info("分页查询:{}", setmealPageQueryDTO);

        PageResult pageResult = setMealService.page(setmealPageQueryDTO);

        return Result.success(pageResult);
    }

    @ApiOperation("批量删除套餐")
    @DeleteMapping()
    @CacheEvict(cacheNames = "userSetMeal",allEntries = true)
    public Result delete(@RequestParam List<Long> ids)
    {
        log.info("批量删除套餐:{}",ids);

        setMealService.delete(ids);

        return Result.success();
    }

    @ApiOperation("根据id查询套餐")
    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id)
    {
        log.info("根据id查询套餐:{}",id);

        SetmealVO setmealVO = setMealService.getById(id);

        return Result.success(setmealVO);
    }

    @ApiOperation("修改套餐")
    @PutMapping()
    @CacheEvict(cacheNames = "userSetMeal",allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO)
    {
        log.info("修改套餐:{}",setmealDTO);

        setMealService.update(setmealDTO);

        return Result.success();

    }

    @ApiOperation("套餐起售、停售")
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "userSetMeal",allEntries = true)
    public Result startOrDown(@PathVariable Integer status,Long id)
    {
        log.info("套餐起售、停售:{},{}",status,id);

        setMealService.startOrDown(status,id);

        return Result.success();

    }






}
