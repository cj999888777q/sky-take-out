package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关")
public class DishController {


       @Autowired
       private DishService dishService;
       @Autowired
       private RedisTemplate redisTemplate;


       @ApiOperation("新增菜品")
       @PostMapping()
       public Result save(@RequestBody DishDTO dishDTO)
       {
           log.info("新增菜品:{}",dishDTO);

           Long categoryId = dishDTO.getCategoryId();

           String key = "dish_"+categoryId;

           redisTemplate.delete(key);
           dishService.save(dishDTO);
           return Result.success();
       }

       @ApiOperation("分页查询菜品")
       @GetMapping("/page")
       public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO)
       {
           PageResult pageResult = dishService.page(dishPageQueryDTO);
           return Result.success(pageResult);
       }

       @ApiOperation("批量删除菜品")
       @DeleteMapping()
       public Result delete(@RequestParam List<Long> ids)
       {
           log.info("批量删除菜品:{}",ids);
           dishService.delete(ids);
           clean("dish_*");
           return Result.success();
       }

       @ApiOperation("根据id查询菜品")
       @GetMapping("/{id}")
       public Result<DishVO> getById(@PathVariable Long id)
       {
           log.info("根据id查询菜品:{}",id);
           DishVO dishVO = dishService.getById(id);
           return Result.success(dishVO);
       }


       @ApiOperation("修改菜品")
       @PutMapping
       public Result update(@RequestBody DishDTO dishDTO)
       {
           log.info("修改菜品:{}",dishDTO);

           dishService.update(dishDTO);

           clean("dish_*");

           return Result.success();
       }

       @ApiOperation("菜品起售，停售")
       @PostMapping("/status/{status}")
       public Result startOrStop(@PathVariable Integer status,Long id)
       {
           log.info("菜品起售,停售:{},{}",status,id);

           dishService.startOrStop(status,id);

           clean("dish_*");

           return Result.success();
       }

       @ApiOperation("根据分类id查询菜品")
       @GetMapping("/list")
       public Result<List<Dish>> getList(Long categoryId)
       {
           log.info("根据分类id查询菜品:{}",categoryId);

           List<Dish> list = dishService.getList(categoryId);

           return Result.success(list);
       }


    /**
     * 清理缓存
     */
    public void clean(String h)
    {
        Set keys = redisTemplate.keys(h);

        redisTemplate.delete(keys);
    }


}
