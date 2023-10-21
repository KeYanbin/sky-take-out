package com.sky.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 分类业务层
 */
@Service
@Slf4j
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    /**
     * 修改分类
     *
     * @param categoryDTO
     * @return
     */
    @Override
    public void updateCategory(CategoryDTO categoryDTO) {
        updateById(BeanUtil.copyProperties(categoryDTO, Category.class));
    }

    /**
     * 分类分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        Page<Category> page = Page.of(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        IPage<Category> p = lambdaQuery()
                .eq(categoryPageQueryDTO.getType() != null, Category::getType, categoryPageQueryDTO.getType())
                .like(StrUtil.isNotBlank(categoryPageQueryDTO.getName()), Category::getName, categoryPageQueryDTO.getName())
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime)
                .page(page);

        return new PageResult(p.getTotal(), p.getRecords());
    }

    /**
     * 启用、禁用分类
     *
     * @param status 状态
     * @param id     分类id
     * @return
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // status 如果是0 则禁用
        lambdaUpdate().eq(Category::getId, id).set(Category::getStatus, status).update();
    }

    /**
     * 新增分类
     *
     * @param categoryDTO
     * @return
     */
    @Override
    public void saveCategory(CategoryDTO categoryDTO) {
        save(BeanUtil.copyProperties(categoryDTO, Category.class));
    }

    /**
     * 删除分类
     *
     * @param id
     * @return
     */
    @Override
    public void deleteById(Long id) {
        //查询当前分类是否关联了菜品，如果关联了就抛出业务异常
        Integer count = Db.lambdaQuery(Dish.class).eq(Dish::getCategoryId, id).list().size();

        if (count > 0) {
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_DISH);
        }

        //查询当前分类是否关联了套餐，如果关联了就抛出业务异常
        count = Db.lambdaQuery(Setmeal.class).eq(Setmeal::getCategoryId, id).list().size();
        if (count > 0) {
            //当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        //删除分类数据
        removeById(id);
    }

    /**
     * 根据类型查询分类
     *
     * @param type
     * @return
     */
    @Override
    public List<Category> getCategoryAllByType(Integer type) {
        return lambdaQuery().eq(Category::getStatus, StatusConstant.ENABLE)
                .eq(Category::getType, type)
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime)
                .list();
    }

    @Override
    public List<Category> getList(Integer type) {
        return lambdaQuery().eq(type != null, Category::getType, type).list();
    }
}
