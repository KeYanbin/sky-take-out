package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 套餐(Setmeal)表数据库访问层
 *
 * @author keyanbin
 * @since 2023-10-07 19:47:05
 */
public interface SetmealMapper extends BaseMapper<Setmeal> {

    Page<SetmealVO> pageQuery(Page<Setmeal> page, @Param("ew") QueryWrapper<Setmeal> like);

    List<DishItemVO> getDishItemById(@Param("ew") QueryWrapper<SetmealDish> wrapper);
}

