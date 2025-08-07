package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/setmeal")
@Api(value = "套餐相关接口")
@Slf4j
public class SetMealController {
    @Autowired
    private SetmealService setMealService;

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult>page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("分页查询 {}",setmealPageQueryDTO);
        PageResult pageResult=setMealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result getById(@PathVariable String id){
        log.info("根据id查询套餐 id：{}",id);
        SetmealVO setmealVO=setMealService.getById(id);
        return Result.success(setmealVO);
    }

    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmealCache",key="#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐{}",setmealDTO);
        setMealService.saveWithSetmealDish(setmealDTO);
        return Result.success();

    }


    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmealCache",key="#setmealDTO.categoryId")
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐{}",setmealDTO);
        setMealService.updateWithSetmealDish(setmealDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除套餐")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam String ids){
        log.info("删除套餐{}",ids);
        setMealService.deleteBitchWithSetmealDish(ids);
        return Result.success();
    }


    @PostMapping("/status/{status}")
    @ApiOperation("套餐的起售和停售")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result setStatus(@PathVariable String status,String id){
        log.info("套餐的起售和停售 id={}",id);

        setMealService.setStatus(status,id);

        return Result.success();

    }




}
