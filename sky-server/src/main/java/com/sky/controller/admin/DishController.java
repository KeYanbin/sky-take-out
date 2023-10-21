package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 菜品 前端控制器
 * </p>
 *
 * @author keyanbin
 * @since 2023-10-08
 */
@Slf4j
@RestController
@Api(tags = "菜品相关接口")
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDTO 新增菜品菜数据
     * @return 后端统一返回结果
     */
    @ApiOperation("新增菜品")
    @PostMapping
    public Result addDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品:{}", dishDTO);
        dishService.addDish(dishDTO);
        return Result.success();
    }

    /**
     * 分页查询
     *
     * @param dishPageQueryDTO 菜dto
     * @return Result < page Result >
     */
    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品数据：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 删除
     *
     * @param ids id
     * @return 后端统一返回结果
     */
    @ApiOperation("删除菜品")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除菜品：{}", ids);
        dishService.delete(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     *
     * @param id id
     * @return 结果<DishVO>
     */
    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> selectById(@PathVariable Long id) {
        log.info("根据id查询菜品id:{}", id);
        return Result.success(dishService.selectById(id));
    }

    /**
     * 按id更新菜品
     *
     * @param dishDTO 菜dto
     * @return 后端统一返回结果
     */
    @ApiOperation("修改菜品信息")
    @CacheEvict(cacheNames = "dish_", allEntries = true)
    @PutMapping
    public Result updateDishById(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品信息：{}", dishDTO);
        dishService.updateDishById(dishDTO);
        return Result.success();
    }

    @ApiOperation("菜品起售、停售")
    @CacheEvict(cacheNames = "dish_", allEntries = true)
    @PostMapping("/status/{status}")
    public Result updateDishStatusById(@PathVariable Integer status, Long id) {
        log.info("根据状态更新菜品状态：{}", status);
        dishService.updateDishStatusById(status, id);

        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishService.listByCategoryId(categoryId);
        return Result.success(list);
    }



}
