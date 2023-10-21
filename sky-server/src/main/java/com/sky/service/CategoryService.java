package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 修改分类
     *
     * @param categoryDTO
     * @return
     */
    void updateCategory(CategoryDTO categoryDTO);

    /**
     * 分类分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     * @return
     */
    void startOrStop(Integer status, Long id);


    /**
     * 新增分类
     *
     * @param categoryDTO
     * @return
     */
    void saveCategory(CategoryDTO categoryDTO);

    /**
     * 删除分类
     *
     * @param id
     * @return
     */
    void deleteById(Long id);

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    List<Category> getCategoryAllByType(Integer type);


    List<Category> getList(Integer type);
}
