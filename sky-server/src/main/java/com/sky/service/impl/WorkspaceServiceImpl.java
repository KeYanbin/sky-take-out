package com.sky.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.User;
import com.sky.mapper.OrdersMapper;
import com.sky.service.IOrdersService;
import com.sky.service.UserService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {
    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private IOrdersService ordersService;
    @Autowired
    private UserService userService;

    /**
     * 工作台今日数据查询
     *
     * @return
     */
    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin,LocalDateTime end) {
//        // 获取当天的开始和结束时间
//        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
//        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        LambdaQueryWrapper<Orders> OLW = new LambdaQueryWrapper<>();

        // 每日订单数
        Integer dailyOrderCount = Math.toIntExact(ordersService.count(OLW.between(Orders::getOrderTime, begin, end)));

        // 有效订单数
        Integer validOrderCount = Math.toIntExact(ordersService.count(OLW.eq(Orders::getStatus, Orders.COMPLETED)));

        // 每日营业额
        Double dailyTurnover = ordersMapper.GettheDailyTurnover(OLW);
        dailyTurnover = dailyTurnover == null ? 0.0 : dailyTurnover;

        // 订单完成率
        double orderCompletionRate = dailyOrderCount == 0 ? 0.0 : validOrderCount.doubleValue() / dailyOrderCount;

        // 平均客单价
        Double unitPrice = validOrderCount == 0 ? 0.0 : dailyTurnover / validOrderCount;


        // 新增用户数
        Integer newUsers = Math.toIntExact(userService.count(new LambdaQueryWrapper<User>().between(User::getCreateTime, begin, end)));

        return BusinessDataVO.builder()
                .turnover(dailyTurnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(Double.valueOf(new DecimalFormat("#.00").format(unitPrice)))
                .newUsers(newUsers)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return SetmealOverViewVO
     */
    @Override
    public SetmealOverViewVO getSetmealOverView() {
        // 查询Setmeal表中的所有数据
        Long totalSetmeals = Db.lambdaQuery(Setmeal.class).count();

        Long sold = Db.lambdaQuery(Setmeal.class)
                .eq(Setmeal::getStatus, StatusConstant.ENABLE)
                .count();

        return SetmealOverViewVO.builder()
                .sold(Math.toIntExact(sold))
                .discontinued((int) (totalSetmeals - sold))
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    @Override
    public DishOverViewVO getDishOverView() {
        LambdaQueryChainWrapper<Dish> lambdaQuery = Db.lambdaQuery(Dish.class);

        // 计算总数量
        Integer count = Math.toIntExact(lambdaQuery.count());

        // 计算已售数量
        Integer sold = Math.toIntExact(lambdaQuery.eq(Dish::getStatus, StatusConstant.ENABLE).count());

        return DishOverViewVO.builder().sold(sold).discontinued(count - sold).build();
    }

    /**
     * 查询订单管理数据
     *
     * @return
     */
    @Override
    public OrderOverViewVO getOrderOverView() {
        // 全部订单
        List<Orders> ordersList = Db.lambdaQuery(Orders.class).list();

        // 使用Map存储各种状态的订单数量
        Map<Integer, Long> statusCount = ordersList.stream()
                .collect(Collectors.groupingBy(Orders::getStatus, Collectors.counting()));

        // 待接单
        Integer waitingOrders = Math.toIntExact(statusCount.getOrDefault(Orders.TO_BE_CONFIRMED, 0L));
        // 待派送数量
        Integer deliveredOrders = Math.toIntExact(statusCount.getOrDefault(Orders.CONFIRMED, 0L));
        // 已完成数量
        Integer completedOrders = Math.toIntExact(statusCount.getOrDefault(Orders.COMPLETED, 0L));
        // 已取消
        Integer cancelledOrders = Math.toIntExact(statusCount.getOrDefault(Orders.CANCELLED, 0L));

        return OrderOverViewVO
                .builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(ordersList.size())
                .build();

    }
}
