package com.sky.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.enums.WriteDirectionEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrdersMapper;
import com.sky.service.IOrdersService;
import com.sky.service.ReportService;
import com.sky.service.UserService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private IOrdersService ordersService;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 统计指定时间的营业额
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 日期集合
        ArrayList<LocalDate> dateList = getLocalDates(begin, end);
        // 每日营业额集合
        ArrayList<Double> turnoverList = new ArrayList<>();


        dateList.stream().forEach(date -> {
            LambdaQueryWrapper<Orders> lw = new LambdaQueryWrapper<>();
            lw.eq(Orders::getStatus, Orders.COMPLETED);
            // 获取每天的开始时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            // 明天的结束时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            lw.between(Orders::getOrderTime, beginTime, endTime);
            // 获取每天的营业额
            Double dailyTurnover = ordersMapper.GettheDailyTurnover(lw);
            // 营业额为null 修改为0.0
            dailyTurnover = dailyTurnover == null ? 0.0 : dailyTurnover;
            turnoverList.add(dailyTurnover);
        });

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户数据
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return Result < user report vo>
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 日期集合
        ArrayList<LocalDate> dateList = getLocalDates(begin, end);
        // 用户总量
        ArrayList<Integer> totalUserLists = new ArrayList<>();
        // 新增用户
        ArrayList<Integer> newUserList = new ArrayList<>();


        dateList.stream().forEach(date -> {
            // 每天的开始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 构建条件
            LambdaQueryWrapper<User> lw = new LambdaQueryWrapper<>();
            // 获取用户总量
            Integer total = Math.toIntExact(userService.count(lw.le(User::getCreateTime, endTime)));
            totalUserLists.add(total == null ? 0 : total);

            // 获取每天注册用户数量
            Integer newTotal = Math.toIntExact(userService.count(lw.ge(User::getCreateTime, beginTime)));
            newUserList.add(newTotal == null ? 0 : newTotal);
        });


        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserLists, ","))
                .build();
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //日期
        ArrayList<LocalDate> dateList = getLocalDates(begin, end);
        //每日订单数
        ArrayList<Integer> orderCountList = new ArrayList<>();
        //每日有效订单数
        ArrayList<Integer> validOrderCountList = new ArrayList<>();

        dateList.stream().forEach(date -> {
            // 每天的开始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            LambdaQueryWrapper<Orders> lw = new LambdaQueryWrapper<>();
            // 每日订单数
            Integer DailyOrderQuantity = Math.toIntExact(ordersService.count(lw.between(Orders::getOrderTime, beginTime, endTime)));
            orderCountList.add(DailyOrderQuantity == null ? 0 : DailyOrderQuantity);

            // 有效订单
            Integer NumberOfValidOrdersPerDay = Math.toIntExact(ordersService.count(lw.eq(Orders::getStatus, Orders.COMPLETED)));
            validOrderCountList.add(NumberOfValidOrdersPerDay == null ? 0 : NumberOfValidOrdersPerDay);

        });

        // 订单总数 -> 每日订单数总和
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        // 订单完成率
        double orderCompletionRate = 0.0;
        // 避免分母为0的情况
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

    }

    /**
     * top10
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return SalesTop10ReportVO
     */
    @Override
    public SalesTop10ReportVO top10Sales(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> list = ordersMapper.getSalesTop(beginTime, endTime);

        //商品名称列表
        String nameList = list.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.joining(","));
        //销量列表
        String numberList = list.stream()
                .map(dto -> dto.getNumber().toString())
                .collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出Excel报表
     *
     * @param response
     */
    @Override
    public void export(HttpServletResponse response) throws IOException {
        // 获取过去30天的开始和结束日期
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        // 获取过去30天的业务数据
        List<BusinessDataVO> businessDataVOS = getBusinessDataForPeriod(begin, end);

        // 获取过去30天每天的业务数据
        List<BusinessDataVO> dailyData = getDailyBusinessDataForPeriod(begin, end);

        // 将数据写入Excel文件,并将Excel文件导出到浏览器
        writeBusinessDataToExcel(businessDataVOS, dailyData, response);
    }

    /**
     * 获取过去30天的业务数据
     *
     * @param begin
     * @param end
     * @return
     */
    private List<BusinessDataVO> getBusinessDataForPeriod(LocalDate begin, LocalDate end) {
        List<BusinessDataVO> businessDataVOS = new ArrayList<>();
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        businessData.setData("时间：" + begin + "至" + end);
        businessDataVOS.add(businessData);
        return businessDataVOS;
    }

    /**
     * 获取过去30天每天的业务数据
     *
     * @param begin
     * @param end
     * @return
     */
    private List<BusinessDataVO> getDailyBusinessDataForPeriod(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> localDates = getLocalDates(begin, end);
        List<BusinessDataVO> dailyData = new ArrayList<>(localDates.size());
        for (LocalDate localDate : localDates) {
            BusinessDataVO businessDailyData = workspaceService.getBusinessData(LocalDateTime.of(localDate, LocalTime.MIN), LocalDateTime.of(localDate, LocalTime.MAX));
            businessDailyData.setData(localDate.toString());
            dailyData.add(businessDailyData);
        }
        return dailyData;
    }

    /**
     * 将数据写入Excel文件,并将Excel文件导出到浏览器
     *
     * @param businessDataVOS
     * @param dailyData
     * @param response
     * @throws IOException
     */
    private void writeBusinessDataToExcel(List<BusinessDataVO> businessDataVOS, List<BusinessDataVO> dailyData, HttpServletResponse response) throws IOException {
        InputStream templateFileName = new ClassPathResource("/template/运营数据报表模板.xlsx").getInputStream();
        ServletOutputStream fileName = response.getOutputStream();

        try (ExcelWriter excelWriter = EasyExcel.write(fileName).withTemplate(templateFileName).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            FillConfig fillConfig = FillConfig.builder().direction(WriteDirectionEnum.HORIZONTAL).build();
            excelWriter.fill(new FillWrapper("list", businessDataVOS), fillConfig, writeSheet);
            excelWriter.fill(new FillWrapper("DailyData", dailyData), writeSheet);
        }
    }

    /**
     * 得到某个日期范围内的所有日期
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return ArrayList<OrderTotal>
     */
    private static ArrayList<LocalDate> getLocalDates(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}
