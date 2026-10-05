package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishSetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private DishSetmealMapper dishSetmealMapper;
    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional
    public void addDish(DishDTO dishDTO) {
        //Insert菜品
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        List<DishFlavor> df = dishDTO.getFlavors();
        if (df != null || df.size() > 0) {
            for (DishFlavor dishFlavor : df) {
                dishFlavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insert(df);
        }
    }
    @Override
    public PageResult pageDish(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page= dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Transactional
    @Override
    public void deleteDish(List<Long> ids) {
        //检查菜品是否在售
        for (Long id : ids) {
            Dish dish = dishMapper.queryById(id);
            if (dish != null && dish.getStatus() == 1) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //检查是否存在套餐
        List<Long> setmealIds = dishSetmealMapper.queryByIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除关联口味表
        for (Long id : ids) {
            dishFlavorMapper.deleteByDishId(id);
        }
        //删除菜品
        dishMapper.deleteBatch(ids);
    }
}
