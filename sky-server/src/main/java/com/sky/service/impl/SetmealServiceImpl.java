package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page=setmealMapper.pageQuery(setmealPageQueryDTO);
        Long total=page.getTotal();
        List<Setmeal> records=page.getResult();
        return new PageResult(total,records);
    }

    @Override
    public SetmealVO getById(String id) {
        SetmealVO setmealVO =new SetmealVO();
        List<SetmealDish> setmealDishes=setmealDishMapper.getBySetmealId(id);
        Setmeal setmeal=setmealMapper.getById(id);
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void saveWithSetmealDish(SetmealDTO setmealDTO) {
       Setmeal setmeal= new Setmeal();
       BeanUtils.copyProperties(setmealDTO,setmeal);
       List<SetmealDish>setmealDishes=setmealDTO.getSetmealDishes();

       setmealMapper.add(setmeal);

       for(SetmealDish dish:setmealDishes){
            dish.setSetmealId(setmeal.getId());
           setmealDishMapper.add(dish);
       }
    }

    @Override
    public void updateWithSetmealDish(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        setmealDishMapper.deleteBySetmealId(setmeal.getId());

        for(SetmealDish setmealDish:setmealDTO.getSetmealDishes()){
            setmealDish.setSetmealId(setmeal.getId());
            setmealDishMapper.add(setmealDish);
        }
    }

    @Override
    public void deleteBitchWithSetmealDish(String ids) {
        String[] idArr=ids.split(",");

        for(String id:idArr){
            if(setmealMapper.getById(id).getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }


        }

        if(idArr!=null&idArr.length>0){
            for(String id:idArr){
                setmealDishMapper.deleteBySetmealId(Long.valueOf(id));
                setmealMapper.delete(Long.valueOf(id));
            }


        }
    }

    @Override
    public void setStatus(String status, String id) {
        setmealMapper.setStatus(status,id);
    }


    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
