package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 套餐(Setmeal)表服务接口
 *
 * @author keyanbin
 * @since 2023-10-07 19:47:05
 */
public interface SetmealService extends IService<Setmeal> {

    /**
     * 分页查询
     *
     * @param pageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO pageQueryDTO);

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDTO
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐，用于修改页面回显数据
     *
     * @param id
     * @return
     */
    SetmealVO getSetmealById(Long id);

    /**
     * 修改套餐
     *
     * @param setmealDTO
     * @return
     */
    void updateSetmealById(SetmealDTO setmealDTO);

    /**
     * 批量删除套餐
     *
     * @param ids
     * @return
     */
    void deleteBatch(List<Long> ids);

    /**
     * 套餐起售停售
     * @param status
     * @param id
     * @return
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据分类id查询套餐
     *
     * @param categoryId categoryId
     * @return
     */
    List<Setmeal> getList(Long categoryId);

    /**
     * 根据套餐id查询包含的菜品列表
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}

