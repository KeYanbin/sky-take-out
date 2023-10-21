package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜品(Dish)表服务接口
 *
 * @author keyanbin
 * @since 2023-10-07 19:38:02
 */
public interface DishService extends IService<Dish> {

    /**
     * 新增菜品和对应口味
     *
     * @param dishDTO 新增菜品菜数据
     * @return 后端统一返回结果
     */
    void addDish(DishDTO dishDTO);

    /**
     * 分页查询
     *
     * @param dishPageQueryDTO 菜dto
     * @return Result < page Result >
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void delete(List<Long> ids);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    DishVO selectById(Long id);


    void updateDishById(DishDTO dishDTO);


    void updateDishStatusById(Integer status, Long id);

    List<Dish> listByCategoryId(Long categoryId);

    List<DishVO> listWithFlavor(Long categoryId);

}

