package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 套餐(Setmeal)表服务实现类
 *
 * @author keyanbin
 * @since 2023-10-07 19:47:05
 */
@Service("setmealService")
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 分页查询
     *
     * @param pageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO) {
        Page<Setmeal> page = Page.of(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        String name = pageQueryDTO.getName();
        Integer status = pageQueryDTO.getStatus();
        Integer categoryId = pageQueryDTO.getCategoryId();

        Page<SetmealVO> p = setmealMapper.pageQuery(page, new QueryWrapper<Setmeal>()
                .eq(pageQueryDTO.getStatus() != null, "s.status", status)
                .eq(pageQueryDTO.getCategoryId() != null, "s.category_id", categoryId)
                .like(pageQueryDTO.getName() != null, "s.name", name)
        );

        return new PageResult(p.getTotal(), p.getRecords());
    }

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);

        //向套餐表插入数据
        save(setmeal);

        //获取生成的套餐id
        Long categoryId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(sd -> sd.setSetmealId(categoryId));

        //保存套餐和菜品的关联关系
        Db.saveBatch(setmealDishes);


    }

    /**
     * 根据id查询套餐，用于修改页面回显数据
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getSetmealById(Long id) {
        Setmeal setmeal = getById(id);

        List<SetmealDish> setmealDishList = Db.lambdaQuery(SetmealDish.class).eq(SetmealDish::getSetmealId, id).list();

        SetmealVO setmealVO = BeanUtil.copyProperties(setmeal, SetmealVO.class);
        setmealVO.setSetmealDishes(setmealDishList);

        return setmealVO;
    }

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    @Override
    @Transactional
    public void updateSetmealById(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);

        //1、修改套餐表，执行update
        updateById(setmeal);

        //2、删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        Long id = setmeal.getId();
        Db.lambdaUpdate(SetmealDish.class).eq(SetmealDish::getSetmealId, id).remove();

        //3、重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        setmealDTO.getSetmealDishes().forEach(s -> s.setSetmealId(id));
        Db.saveBatch(setmealDTO.getSetmealDishes());
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //起售中的套餐不能删除
        List<Setmeal> setmealList = lambdaQuery().in(Setmeal::getId, ids).list();

        List<Long> list = setmealList.stream()
                .filter(s -> s.getStatus().equals(StatusConstant.DISABLE))
                .map(Setmeal::getId)
                .toList();


        if (list.size() == 0) {
            //起售中的套餐不能删除
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        removeBatchByIds(list);
        Db.lambdaUpdate(SetmealDish.class).in(SetmealDish::getSetmealId, list).remove();

    }

    /**
     * 套餐起售停售
     *
     * @param status 状态
     * @param id     套餐id
     * @return
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        lambdaUpdate().eq(Setmeal::getId, id).set(Setmeal::getStatus, status).update();
    }

    @Override
    public List<Setmeal> getList(Long categoryId) {
        return lambdaQuery()
                .eq(Setmeal::getCategoryId, categoryId)
                .eq(Setmeal::getStatus, StatusConstant.ENABLE)
                .list();
    }


    /**
     * 根据套餐id查询包含的菜品列表
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        QueryWrapper<SetmealDish> wrapper = new QueryWrapper<SetmealDish>().eq("sd.setmeal_id", id);

        return setmealMapper.getDishItemById(wrapper);

    }


}

