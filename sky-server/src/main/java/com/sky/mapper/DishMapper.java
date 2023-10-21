package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜品(Dish)表数据库访问层
 *
 * @author keyanbin
 * @since 2023-10-07 19:38:02
 */
public interface DishMapper extends BaseMapper<Dish> {

    Page<DishVO> pageQuery(IPage<DishVO> page, @Param("ew") QueryWrapper<Dish> eq);
}

