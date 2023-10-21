package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author keyanbin
 * @since 2023-10-14
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update_1(Orders orders);


    @Select("select sum(amount) from orders ${ew.customSqlSegment} ")
    Double GettheDailyTurnover(@Param("ew") LambdaQueryWrapper<Orders> wrapper);

    List<GoodsSalesDTO> getSalesTop(LocalDateTime beginTime, LocalDateTime endTime);
}
