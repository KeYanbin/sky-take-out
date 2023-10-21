package com.sky.task;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.sky.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务
 */
@Slf4j
@Component
public class OrderTask {

    private final Integer TIMEOUT_MINUTES = -15;
    private final Integer DELIVERY_MINUTES = -60;

    /**
     * 处理超时订单，一分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单任务 {}", LocalDateTime.now());

        // 当前时间 + -15
        LocalDateTime time = LocalDateTime.now().plusMinutes(TIMEOUT_MINUTES);

        // 查询超时订单
        List<Orders> ordersList = Db.lambdaQuery(Orders.class)
                .eq(Orders::getStatus, Orders.PENDING_PAYMENT)
                .lt(Orders::getOrderTime, time)
                .list();
        if (ordersList != null && ordersList.size() > 0) {
            ordersList.forEach(orders -> {
                // 订单状态设置为取消
                orders.setStatus(Orders.CANCELLED);
                // 取消原因
                orders.setCancelReason("支付超时");
                // 取消时间
                orders.setOrderTime(LocalDateTime.now());
            });
            Db.updateBatchById(ordersList);
        }
    }

    /**
     * 处理一直在派送中订单 每天凌晨1点执行
     */
    @Scheduled(cron = "0 0 1 * * ?")
//    @Scheduled(cron = "0/5 * * * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理派送中订单任务 {}", LocalDateTime.now());
        // 查询超时订单
        List<Orders> ordersList = Db.lambdaQuery(Orders.class)
                .eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                .le(Orders::getOrderTime, LocalDateTime.now().plusMinutes(DELIVERY_MINUTES))
                .list();
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
            }

            Db.updateBatchById(ordersList);
        }

    }
}
