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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;


    @PostMapping
    @ApiOperation("新增菜品")
    @CacheEvict(cacheNames = "userDish",key="#dishDTO.categoryId")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品");
        dishService.saveWithFlavor(dishDTO);
        return Result.success("添加成功");
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询 {}", dishPageQueryDTO);
       PageResult pageResult= dishService.pageQuery(dishPageQueryDTO);
       return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("菜品批量删除")
    @CacheEvict(cacheNames = "userDish",allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
            log.info("菜品删除{}",ids);
            dishService.deleteBitch(ids);
            return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品{}",id);
        DishVO dishVO=dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }


    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "userDish",allEntries = true)
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息");
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result getByCategory(@RequestParam String categoryId){
        List<Dish> dishes=dishService.getByCategoryId(categoryId);
        return Result.success(dishes);
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    @CacheEvict(cacheNames = "userDish",allEntries = true)
    public Result status(@PathVariable String status ,@RequestParam String id ){
        log.info("菜品停售起售");
        dishService.setStatus(status,id);
        return Result.success();
    }
}
