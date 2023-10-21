package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.query.PageQuery;
import com.sky.result.PageResult;
import com.sky.service.IOrdersService;
import com.sky.utils.BaiduApiUtils;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author keyanbin
 * @since 2023-10-14
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    // 商家地址
    @Value("${sky.shop.address}")
    private String shopAddress;

    // 百度 AK
    @Value("${sky.baidu.ak}")
    private String AK;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrdersMapper orderMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理各种业务异常
        // 判断订单地址是否为空
        AddressBook addressBook = Db.getById(ordersSubmitDTO.getAddressBookId(), AddressBook.class);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 检查用户的收货地址是否超出配送范围
        // 用户收货地址
        String address = addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail();
        // 用户收货地址经纬度
        String coordinate = BaiduApiUtils.getCoordinate(address, AK);
        // 计算距离
        Integer distance = BaiduApiUtils.getDistance(shopAddress, coordinate, AK);

        //配送距离超过5000米
        if (distance > 50000) {
            throw new OrderBusinessException("超出配送范围");
        }

        // 获取用户id
        Long userId = BaseContext.getCurrentId();

        // 购物车数据是否为空
        List<ShoppingCart> shoppingCartList = Db.lambdaQuery(ShoppingCart.class)
                .eq(ShoppingCart::getUserId, userId)
                .list();
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 订单表插入一条数据
        Orders orders = BeanUtil.copyProperties(ordersSubmitDTO, Orders.class);
        // 设置用户id
        orders.setUserId(userId);
        //  设置付款状态 未支付
        orders.setPayStatus(Orders.UN_PAID);
        //  设置订单状态 待付款
        orders.setStatus(Orders.PENDING_PAYMENT);
        // 设置订单号 以当前时间戳
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        // 设置手机号
        orders.setPhone(addressBook.getPhone());
        // 设置收货人
        orders.setConsignee(addressBook.getConsignee());
        // 设置收货地址
        orders.setAddress(address);
        save(orders);

        // 订单明细表插入提交的商品数据，要查购物车数据
        List<OrderDetail> orderDetailList = BeanUtil.copyToList(shoppingCartList, OrderDetail.class);
        // 设置当前订单明细关联的订单id
        orderDetailList.forEach(orderDetail -> orderDetail.setOrderId(orders.getId()));

        Db.saveBatch(orderDetailList);

        // 清空购物车数据
        Db.lambdaUpdate(ShoppingCart.class).eq(ShoppingCart::getUserId, userId).remove();

        // 返回OrderSubmitVO对象
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update_1(orders);

        // 调用websocket 向客户端推送消息
        Map map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + ordersDB.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 查询订单详情
     *
     * @param id 订单id
     * @return OrderVO
     */
    @Override
    public OrderVO QueryOrderDetails(Long id) {
        // 查询订单表
        Orders orders = getById(id);

        // 查询与订单id关联的订单明细表
        List<OrderDetail> orderDetailList = Db.lambdaQuery(OrderDetail.class).eq(OrderDetail::getOrderId, id).list();

        OrderVO orderVO = BeanUtil.copyProperties(orders, OrderVO.class);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 查询历史订单，分页
     *
     * @param pageQuery
     * @return
     */
    @Override
    public PageResult QueryHistoryOrders(PageQuery pageQuery) {
        // 构建分页条件
        Page<Orders> page = pageQuery.toMpPageDefaultSortByOrderTimeDesc();

        Page<Orders> p = lambdaQuery()
                .eq(Orders::getUserId, BaseContext.getCurrentId())
                .eq(pageQuery.getStatus() != null, Orders::getStatus, pageQuery.getStatus())
                .page(page);

        List<OrderVO> list = new ArrayList<>();

        if (p != null && p.getTotal() > 0) {
            list = BeanUtil.copyToList(p.getRecords(), OrderVO.class);

            // 一次性获取所有的orderId
            List<Long> orderIds = list.stream().map(OrderVO::getId).collect(Collectors.toList());

            // 一次性查询所有的OrderDetails
            Map<Long, List<OrderDetail>> orderDetailsMap = Db.lambdaQuery(OrderDetail.class)
                    .in(OrderDetail::getOrderId, orderIds)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId)); //根据id进行分组，id为map的key

            // 为每个OrderVO设置OrderDetails
            for (OrderVO orderVO : list) {
                orderVO.setOrderDetailList(orderDetailsMap.get(orderVO.getId()));
            }
        }

        return new PageResult(p.getTotal(), list);
    }

    /**
     * 用户取消订单
     *
     * @return
     */
    @Override
    @Transactional
    public void cancel(Long id) {
        // 查询订单
        Orders orders = getById(id);

        // 校验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        // 待支付和待接单状态下，用户可直接取消订单
        // 商家已接单状态下，用户取消订单需电话沟通商家
        // 派送中状态下，用户取消订单需电话沟通商家
        // 如果在待接单状态下取消订单，需要给用户退款
        // 取消订单后需要将订单状态修改为“已取消”

        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //订单状态修改为取消，支付状态修改退款
        orders.setStatus(Orders.CANCELLED);
        orders.setPayStatus(Orders.REFUND);

        // 更新 取消原因、取消时间
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());

        updateById(orders);

    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    @Transactional
    public void AnotherOrder(Long id) {
        List<OrderDetail> orderDetailList = Db.lambdaQuery(OrderDetail.class)
                .eq(OrderDetail::getOrderId, id)
                .list();

        Long userId = BaseContext.getCurrentId();

        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(order -> {
            // 拷贝，忽略id
            ShoppingCart shoppingCart = BeanUtil.copyProperties(order, ShoppingCart.class, "id");
            shoppingCart.setUserId(userId);
            return shoppingCart;
        }).toList();

        Db.saveBatch(shoppingCartList);
    }


    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<Orders> page = ordersPageQueryDTO.toMpPageDefaultSortByOrderTimeDesc();

        Page<Orders> p = lambdaQuery()
                .eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus())
                .like(ordersPageQueryDTO.getNumber() != null, Orders::getNumber, ordersPageQueryDTO.getNumber())
                .like(ordersPageQueryDTO.getPhone() != null, Orders::getPhone, ordersPageQueryDTO.getPhone())
                .ge(ordersPageQueryDTO.getBeginTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getBeginTime())
                .le(ordersPageQueryDTO.getEndTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getEndTime())
                .page(page);

        // 部分业务需要展示订单详情 OrdersVO

        List<OrderVO> list = new ArrayList<>();
        if (p != null && p.getTotal() > 0) {
            list = BeanUtil.copyToList(p.getRecords(), OrderVO.class);
            // 一次性获取所有的订单id
            List<Long> orderIds = list.stream().map(OrderVO::getId).toList();

            // 一次性查询所有的OrderDetails
            Map<Long, List<OrderDetail>> orderDetailsMap = Db.lambdaQuery(OrderDetail.class)
                    .in(OrderDetail::getOrderId, orderIds)
                    .list()
                    .stream()
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId)); //根据id进行分组，id为map的key

            // 为每个 OrderVO 设置 orderDishes
            for (OrderVO orderVO : list) {
                List<String> stringList = orderDetailsMap.get(orderVO.getId())
                        .stream()
                        .map(OrderDetail -> OrderDetail.getName() + "*" + OrderDetail.getNumber())
                        .toList();
                orderVO.setOrderDishes(String.join(",", stringList));
            }
        }

        return new PageResult(p.getTotal(), list);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        //2待接单 3已接单 4派送中
        List<Orders> orders = list();
        // 使用Java 8流进行过滤和计数
        long toBeConfirmed = orders.stream().filter(o -> o.getStatus().equals(Orders.TO_BE_CONFIRMED)).count();
        long confirmed = orders.stream().filter(o -> o.getStatus().equals(Orders.CONFIRMED)).count();
        long deliveryInProgress = orders.stream().filter(o -> o.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)).count();


        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();

        orderStatisticsVO.setToBeConfirmed(Math.toIntExact(toBeConfirmed));
        orderStatisticsVO.setConfirmed(Math.toIntExact(confirmed));
        orderStatisticsVO.setDeliveryInProgress(Math.toIntExact(deliveryInProgress));

        return orderStatisticsVO;
    }

    /**
     * 订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders = getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        List<OrderDetail> orderDetails = Db.lambdaQuery(OrderDetail.class).eq(OrderDetail::getOrderId, id).list();
        if (orderDetails.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        OrderVO orderVO = BeanUtil.copyProperties(orders, OrderVO.class);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /**
     * 接单
     *
     * @return
     */
    @Override
    public void ReceivingOrders(OrdersConfirmDTO ordersConfirmDTO) {
        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        Orders orders = Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build();
        updateById(orders);
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Long id = ordersRejectionDTO.getId();

        Orders orders = lambdaQuery().eq(Orders::getId, id).one();

        // 只有待接单 才能拒单
        if (orders == null || !orders.getStatus().equals(Orders.REFUND)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        // 退款
        if (orders.getPayStatus() == 1) {
            orders.setPayStatus(Orders.REFUND);
        }
        // 修改状态
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        // 更新
        updateById(orders);
    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancellation4order(OrdersCancelDTO ordersCancelDTO) {
        //取消订单其实就是将订单状态修改为“已取消”
        Orders orders = getById(ordersCancelDTO.getId());

        // 退款
        if (orders.getPayStatus() == 1) {
            orders.setPayStatus(Orders.REFUND);
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        updateById(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        //- 派送订单其实就是将订单状态修改为“派送中”
        //- 只有状态为“待派送”的订单可以执行派送订单操作
        Orders orders = getById(id);

        // 校验订单是否存在，并且状态为3
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 更改订单状态为“派送中”
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        updateById(orders);
    }

    /**
     * 完成订单
     *
     * @return
     */
    @Override
    public void complete(Long id) {
        //- 完成订单其实就是将订单状态修改为“已完成”
        //- 只有状态为“派送中”的订单可以执行订单完成操作
        Orders orders = getById(id);
        // 校验订单是否存在，并且状态为4
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        updateById(orders);
    }

    /**
     * 用户催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = getById(id);

        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        Map map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }


}
