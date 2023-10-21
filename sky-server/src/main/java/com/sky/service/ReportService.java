package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

public interface ReportService {

    /**
     * 营业额统计接口
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户数据
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return Result < user report vo>
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return OrderReportVO
     */
    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    /**
     * top10
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return SalesTop10ReportVO
     */
    SalesTop10ReportVO top10Sales(LocalDate begin, LocalDate end);

    /**
     * 导出Excel报表
     *
     * @param response
     */
    void export(HttpServletResponse response) throws IOException;
}
