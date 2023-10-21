package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public interface WorkspaceService {
    /**
     * 工作台今日数据查询
     *
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin,LocalDateTime end);

    /**
     * 查询套餐总览
     *
     * @return
     */
    SetmealOverViewVO getSetmealOverView();

    /**
     * 查询菜品总览
     *
     * @return
     */
    DishOverViewVO getDishOverView();

    /**
     * 查询订单管理数据
     *
     * @return
     */
    OrderOverViewVO getOrderOverView();
}
