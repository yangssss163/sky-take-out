package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DishService {

    void addDish(DishDTO dishDTO);

    PageResult pageDish(DishPageQueryDTO dishPageQueryDTO);

    void deleteDish(List<Long> ids);
}
