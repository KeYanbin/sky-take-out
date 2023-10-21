package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.*;
import com.sky.entity.Orders;
import com.sky.query.PageQuery;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author keyanbin
 * @since 2023-10-14
 */
public interface IOrdersService extends IService<Orders> {

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    OrderVO QueryOrderDetails(Long id);

    /**
     * 查询历史订单，分页
     *
     * @param pageQuery
     * @return
     */
    PageResult QueryHistoryOrders(PageQuery pageQuery);

    /**
     * 用户取消订单
     *
     * @return
     */
    void cancel(Long id);

    /**
     * 再来一单
     *
     * @param id
     */
    void AnotherOrder(Long id);


    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 订单详情
     *
     * @param id
     * @return
     */
    OrderVO details(Long id);

    /**
     * 接单
     *
     * @return
     */
    void ReceivingOrders(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    void cancellation4order(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单
     *
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成订单
     *
     * @return
     */
    void complete(Long id);

    /**
     * 用户催单
     * @param id
     */
    void reminder(Long id);
}
