package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.IShoppingCartService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 购物车 服务实现类
 * </p>
 *
 * @author keyanbin
 * @since 2023-10-14
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements IShoppingCartService {

    @Autowired
    private DishServiceImpl dishService;

    @Autowired
    private SetmealServiceImpl setmealServiceImpl;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前加入购物车的商品是否存在

        ShoppingCart shoppingCart = BeanUtil.copyProperties(shoppingCartDTO, ShoppingCart.class);

        // 获取用户id
        Long userId = BaseContext.getCurrentId();


        Long dishId = shoppingCart.getDishId();
        String dishFlavor = shoppingCart.getDishFlavor();
        Long setmealId = shoppingCart.getSetmealId();

        // 查询购物车表
        ShoppingCart one = lambdaQuery()
                .eq(ShoppingCart::getUserId, userId)
                .eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
                .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId)
                .one();

        // 存在就加数量
        if (one != null) {
            one.setNumber(one.getNumber() + 1);
            updateById(one);
            return;
        }
        // 不存在就新增
        shoppingCart.setUserId(userId);


        // 判断是菜品还是套餐
        if (dishId != null) {
            DishVO dishVO = dishService.selectById(dishId);
            shoppingCart.setName(dishVO.getName());
            shoppingCart.setAmount(dishVO.getPrice());
            shoppingCart.setImage(dishVO.getImage());
        }

        if (setmealId != null) {
            SetmealVO setmeal = setmealServiceImpl.getSetmealById(setmealId);
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setAmount(setmeal.getPrice());
            shoppingCart.setImage(setmeal.getImage());
        }

        shoppingCart.setNumber(1);
        save(shoppingCart);
    }

    /**
     * 查询购物车
     *
     * @return
     */
    @Override
    public List<ShoppingCart> getShoppingCart() {
        // 获取用户id,并查询购物车列表
        return lambdaQuery().eq(ShoppingCart::getUserId, BaseContext.getCurrentId()).list();
    }

    /**
     * 删除购物车中一个商品
     *
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long dishId = shoppingCartDTO.getDishId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        Long setmealId = shoppingCartDTO.getSetmealId();

        // 判断是否为null
        boolean dishCondition = dishId != null;
        boolean flavorCondition = dishFlavor != null;
        boolean setmealCondition = setmealId != null;

        // 查询商品的数量
        ShoppingCart shopping = lambdaQuery()
                .eq(dishCondition, ShoppingCart::getDishId, dishId)
                .eq(flavorCondition, ShoppingCart::getDishFlavor, dishFlavor)
                .eq(setmealCondition, ShoppingCart::getSetmealId, setmealId)
                .one();

        // 不为1就更新数量
        Integer number = shopping.getNumber();

        // 定义公共的更新操作
        LambdaUpdateChainWrapper<ShoppingCart> updateOperation = lambdaUpdate()
                .eq(dishCondition, ShoppingCart::getDishId, dishId)
                .eq(flavorCondition, ShoppingCart::getDishFlavor, dishFlavor)
                .eq(setmealCondition, ShoppingCart::getSetmealId, setmealId);


        if (number != 1) {
            updateOperation.set(ShoppingCart::getNumber, number - 1).update();
            return;
        }

        // 为1就删除数据
        updateOperation.remove();
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        lambdaUpdate().eq(ShoppingCart::getUserId, userId).remove();
    }
}
