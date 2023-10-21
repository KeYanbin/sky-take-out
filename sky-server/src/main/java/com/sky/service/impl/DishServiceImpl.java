package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.*;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品(Dish)表服务实现类
 *
 * @author keyanbin
 * @since 2023-10-07 19:38:02
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增菜品和对应口味
     *
     * @param dishDTO 新增菜品菜数据
     */
    @Override
    @Transactional
    public void addDish(DishDTO dishDTO) {
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        save(dish);
        //插入成功获取主键
        Long id = dish.getId();
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();

        //判断口味数据是否为空,不为空插入
        if (dishFlavors.size() > 0) {
            dishFlavors.forEach(df -> df.setDishId(id));
            Db.saveBatch(dishFlavors);
        }

    }

    /**
     * 分页查询
     *
     * @param dishPageQueryDTO 菜dto
     * @return Result < page Result >
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        Page<DishVO> page = Page.of(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        String name = dishPageQueryDTO.getName();
        Integer status = dishPageQueryDTO.getStatus();
        Integer categoryId = dishPageQueryDTO.getCategoryId();

        Page<DishVO> p = dishMapper.pageQuery(page, new QueryWrapper<Dish>()
                .eq(status != null, "d.status", status)
                .eq(categoryId != null, "d.category_id", categoryId)
                .like(name != null, "d.name", name)
        );

        return new PageResult(p.getTotal(), p.getRecords());


    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        //判断商品是否在出售中
        for (Long id : ids) {
            if (getById(id).getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断是否关联套餐
        List<SetmealDish> setmealDishList = Db.lambdaQuery(SetmealDish.class).in(SetmealDish::getDishId, ids).list();
        if (CollUtil.isNotEmpty(setmealDishList)) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品
        removeBatchByIds(ids);
        //删除关联的口味
        Db.lambdaUpdate(DishFlavor.class).in(DishFlavor::getDishId, ids).remove();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO selectById(Long id) {
        Dish dish = getById(id);

        //根据id查询口味
        List<DishFlavor> flavor = Db.lambdaQuery(DishFlavor.class).eq(DishFlavor::getDishId, id).list();

        //查询菜品分类
        Category category = Db.lambdaQuery(Category.class).eq(Category::getId, dish.getCategoryId()).one();

        DishVO dishVO = BeanUtil.copyProperties(dish, DishVO.class);

        //把口味集合添加VO
        if (CollUtil.isNotEmpty(flavor)) {
            dishVO.setFlavors(flavor);
        }

        dishVO.setCategoryName(category.getName());

        return dishVO;
    }

    @Override
    @Transactional
    public void updateDishById(DishDTO dishDTO) {
        //修改菜品信息
        updateById(BeanUtil.copyProperties(dishDTO, Dish.class));

        //删除所有口味
        Db.lambdaUpdate(DishFlavor.class).eq(DishFlavor::getDishId, dishDTO.getId()).remove();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDTO.getId());
        }
        //添加口味
        Db.saveBatch(flavors);


    }

    @Override
    @Transactional
    public void updateDishStatusById(Integer status, Long id) {
        Dish dish = Dish.builder().id(id).status(status).build();
        //修改状态
        updateById(dish);

        if (status == StatusConstant.ENABLE) {
            return;
        }
        //菜品停售，如果关联了套餐，连套餐也停售
        List<SetmealDish> list = Db.lambdaQuery(SetmealDish.class).eq(SetmealDish::getDishId, id).list();
        if (CollUtil.isNotEmpty(list)) {
            List<Long> ids = list.stream().map(SetmealDish::getSetmealId).collect(Collectors.toList());
            Db.lambdaUpdate(Setmeal.class).set(Setmeal::getStatus, status).in(Setmeal::getId, ids).update();
        }
    }

    @Override
    public List<Dish> listByCategoryId(Long categoryId) {
        return lambdaQuery()
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, StatusConstant.ENABLE)
                .orderByDesc(Dish::getCreateTime)
                .list();
    }

    @Override
    public List<DishVO> listWithFlavor(Long categoryId) {
        List<Dish> dishList = lambdaQuery().eq(Dish::getCategoryId, categoryId).list();

        List<DishVO> dishVO = BeanUtil.copyToList(dishList, DishVO.class);

        for (DishVO vo : dishVO) {
            List<DishFlavor> flavorList = Db.lambdaQuery(DishFlavor.class).eq(DishFlavor::getDishId, vo.getId()).list();
            if (flavorList.size() > 0) {
                vo.setFlavors(flavorList);
            }
        }
        return dishVO;
    }


}

