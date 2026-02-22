package com.sky.controller.admin;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/category")
@Api(tags = "分类相关")
public class CategoryController {


       @Autowired
       private CategoryService categoryService;


       @ApiOperation("分页查询")
       @GetMapping("/page")
       public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO)
       {
           PageResult pageResult = categoryService.page(categoryPageQueryDTO);
           return Result.success(pageResult);
       }

       @ApiOperation("根据类型查询分类")
       @GetMapping("/list")
       public Result<List<Category>> getByList(Integer type)
       {
              List<Category> list = categoryService.getByType(type);
              return Result.success(list);
       }

       @ApiOperation("根据id删除分类")
       @DeleteMapping()
       public Result deleteById(Long id)
       {
           categoryService.deleteById(id);
           return Result.success();
       }

       @ApiOperation("新增分类")
       @PostMapping()
       public Result save(@RequestBody CategoryDTO categoryDTO)
       {
              categoryService.save(categoryDTO);

              return Result.success();
       }

       @ApiOperation("启用禁用分类")
       @PostMapping("/status/{status}")
       public Result upOrDown(@PathVariable Integer status,Long id)
       {
              categoryService.upOrDown(status,id);

              return Result.success();
       }

       @ApiOperation("修改分类")
       @PutMapping()
       public Result update(@RequestBody CategoryDTO categoryDTO)
       {
              categoryService.update(categoryDTO);

              return Result.success();
       }



}
