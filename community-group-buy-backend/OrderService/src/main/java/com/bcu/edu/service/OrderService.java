package com.bcu.edu.service;

import com.bcu.edu.client.LeaderServiceClient;
import com.bcu.edu.client.ProductServiceClient;
import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.CreateOrderRequest;
import com.bcu.edu.dto.response.*;
import com.bcu.edu.entity.OrderItem;
import com.bcu.edu.entity.OrderMain;
import com.bcu.edu.enums.OrderStatus;
import com.bcu.edu.enums.PayStatus;
import com.bcu.edu.repository.OrderItemRepository;
import com.bcu.edu.repository.OrderMainRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 订单服务
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderMainRepository orderMainRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private LeaderServiceClient leaderServiceClient;

    /**
     * 创建订单（供GroupBuyService调用）⭐核心方法
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "创建订单", module = "订单管理")
    public Long createOrder(CreateOrderRequest request) {
        log.info("开始创建订单: userId={}, productId={}, quantity={}", 
                 request.getUserId(), request.getProductId(), request.getQuantity());

        // 1. 验证用户
        Result<Boolean> userValid = userServiceClient.validateUser(request.getUserId());
        if (userValid.getData() == null || !userValid.getData()) {
            throw new BusinessException("用户不存在");
        }

        // 2. 验证地址
        Result<AddressDTO> addressResult = userServiceClient.getAddress(request.getAddressId());
        if (addressResult.getData() == null) {
            throw new BusinessException("收货地址不存在");
        }
        AddressDTO address = addressResult.getData();

        // 3. 验证团长
        Result<Boolean> leaderValid = leaderServiceClient.validateLeader(request.getLeaderId());
        if (leaderValid.getData() == null || !leaderValid.getData()) {
            throw new BusinessException("团长不存在");
        }

        // 4. 生成订单编号
        String orderSn = generateOrderSn();

        // 5. 创建订单主表
        OrderMain order = new OrderMain();
        order.setUserId(request.getUserId());
        order.setLeaderId(request.getLeaderId());
        order.setOrderSn(orderSn);
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setPayStatus(PayStatus.UNPAID.getCode());
        order.setReceiveAddressId(request.getAddressId());

        // 6. 计算订单金额
        BigDecimal totalPrice = request.getPrice().multiply(new BigDecimal(request.getQuantity()));
        order.setTotalAmount(totalPrice);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalPrice);

        // 7. 保存订单主表
        OrderMain savedOrder = orderMainRepository.save(order);
        log.info("订单主表已保存: orderId={}, orderSn={}", savedOrder.getOrderId(), savedOrder.getOrderSn());

        // 8. 创建订单明细（商品快照）
        OrderItem item = new OrderItem();
        item.setOrderId(savedOrder.getOrderId());
        item.setProductId(request.getProductId());
        item.setActivityId(request.getActivityId());
        item.setProductName(request.getProductName());
        item.setProductImg(request.getProductImg());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        item.setTotalPrice(totalPrice);

        orderItemRepository.save(item);
        log.info("订单明细已保存: itemId={}, productId={}", item.getItemId(), item.getProductId());

        return savedOrder.getOrderId();
    }

    /**
     * 批量更新订单状态（供GroupBuyService成团时调用）⭐核心方法
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "批量更新订单状态", module = "订单管理")
    public void batchUpdateStatus(List<Long> orderIds, Integer newStatus) {
        log.info("批量更新订单状态: orderIds={}, status={}", orderIds, newStatus);

        List<OrderMain> orders = orderMainRepository.findAllById(orderIds);
        if (orders.isEmpty()) {
            log.warn("未找到任何订单: orderIds={}", orderIds);
            return;
        }

        orders.forEach(order -> {
            order.setOrderStatus(newStatus);
            order.setUpdateTime(LocalDateTime.now());
        });

        orderMainRepository.saveAll(orders);
        log.info("批量更新完成: 共更新{}条订单", orders.size());
    }

    /**
     * 更新单个订单状态
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "更新订单状态", module = "订单管理")
    public void updateOrderStatus(Long orderId, Integer newStatus) {
        log.info("更新订单状态: orderId={}, status={}", orderId, newStatus);

        OrderMain order = orderMainRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("订单不存在"));

        order.setOrderStatus(newStatus);
        order.setUpdateTime(LocalDateTime.now());

        orderMainRepository.save(order);
        log.info("订单状态已更新: orderId={}, status={}", orderId, newStatus);
    }

    /**
     * 更新支付状态（供PaymentService调用）
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "更新支付状态", module = "订单管理")
    public void updatePayStatus(Long orderId, Integer payStatus) {
        log.info("更新支付状态: orderId={}, payStatus={}", orderId, payStatus);

        OrderMain order = orderMainRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("订单不存在"));

        order.setPayStatus(payStatus);
        if (payStatus.equals(PayStatus.PAID.getCode())) {
            order.setPayTime(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.PENDING_DELIVERY.getCode());
        }
        order.setUpdateTime(LocalDateTime.now());

        orderMainRepository.save(order);
        log.info("支付状态已更新: orderId={}, payStatus={}", orderId, payStatus);
    }

    /**
     * 取消订单（供GroupBuyService退团时调用）⭐核心方法
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "取消订单", module = "订单管理")
    public void cancelOrder(Long orderId) {
        log.info("取消订单: orderId={}", orderId);

        OrderMain order = orderMainRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("订单不存在"));

        // 只有待支付和待发货的订单可以取消
        if (order.getOrderStatus() > OrderStatus.PENDING_DELIVERY.getCode()) {
            throw new BusinessException("订单已发货，无法取消");
        }

        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        order.setUpdateTime(LocalDateTime.now());

        orderMainRepository.save(order);
        log.info("订单已取消: orderId={}", orderId);
    }

    /**
     * 查询订单详情
     */
    public OrderDetailVO getOrderDetail(Long orderId) {
        OrderMain order = orderMainRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("订单不存在"));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderStatusText(OrderStatus.fromCode(order.getOrderStatus()).getDescription());
        vo.setItems(items.stream().map(this::convertToItemVO).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 查询用户订单列表
     */
    public PageResult<OrderVO> getMyOrders(Long userId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderMain> orderPage = orderMainRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);

        List<OrderVO> voList = orderPage.getContent().stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            vo.setOrderStatusText(OrderStatus.fromCode(order.getOrderStatus()).getDescription());

            // 查询订单商品
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            vo.setItems(items.stream().map(this::convertToItemVO).collect(Collectors.toList()));

            // 判断订单类型：如果有商品关联了活动，则为拼团订单
            boolean isGroupBuy = items.stream().anyMatch(item -> item.getActivityId() != null);
            vo.setOrderType(isGroupBuy ? 1 : 0);

            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(orderPage.getTotalElements(), voList);
    }

    /**
     * 管理端：获取订单列表
     */
    public PageResult<OrderVO> getOrderListForAdmin(Integer page, Integer size, 
                                                    Integer status, Integer payStatus) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderMain> orderPage;

        // 根据条件查询
        if (status != null && payStatus != null) {
            orderPage = orderMainRepository.findByOrderStatusAndPayStatusOrderByCreateTimeDesc(
                status, payStatus, pageable);
        } else if (status != null) {
            orderPage = orderMainRepository.findByOrderStatusOrderByCreateTimeDesc(status, pageable);
        } else if (payStatus != null) {
            orderPage = orderMainRepository.findByPayStatusOrderByCreateTimeDesc(payStatus, pageable);
        } else {
            orderPage = orderMainRepository.findAllByOrderByCreateTimeDesc(pageable);
        }

        List<OrderVO> voList = orderPage.getContent().stream().map(this::convertToOrderVO)
            .collect(Collectors.toList());

        return PageResult.of(page, size, orderPage.getTotalElements(), voList);
    }

    /**
     * 管理端：获取订单统计
     */
    public OrderStatisticsVO getOrderStatistics() {
        OrderStatisticsVO statistics = new OrderStatisticsVO();
        
        // 统计各状态订单数量
        statistics.setTotalOrders(orderMainRepository.count());
        statistics.setPendingPayment(orderMainRepository.countByOrderStatus(0));
        statistics.setPendingDelivery(orderMainRepository.countByOrderStatus(1));
        statistics.setInDelivery(orderMainRepository.countByOrderStatus(2));
        statistics.setDelivered(orderMainRepository.countByOrderStatus(3));
        statistics.setCancelled(orderMainRepository.countByOrderStatus(4));
        statistics.setRefunding(orderMainRepository.countByOrderStatus(5));
        statistics.setRefunded(orderMainRepository.countByOrderStatus(6));

        // 统计今日数据
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        statistics.setTodayOrders(orderMainRepository.countTodayOrders(todayStart));
        statistics.setTodaySales(orderMainRepository.sumTodaySales(todayStart));
        
        // 统计总销售额
        statistics.setTotalSales(orderMainRepository.sumTotalSales());

        return statistics;
    }

    /**
     * 管理端：更新订单状态
     */
    public void updateOrderStatusByAdmin(Long orderId, Integer status) {
        OrderMain order = orderMainRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException("订单不存在"));
        
        order.setOrderStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        orderMainRepository.save(order);
        
        log.info("管理员更新订单状态成功: orderId={}, status={}", orderId, status);
    }

    /**
     * 管理端：搜索订单
     */
    public PageResult<OrderVO> searchOrders(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderMain> orderPage = orderMainRepository.searchByOrderSn(keyword, pageable);

        List<OrderVO> voList = orderPage.getContent().stream().map(this::convertToOrderVO)
            .collect(Collectors.toList());

        return PageResult.of(page, size, orderPage.getTotalElements(), voList);
    }

    /**
     * 管理端：获取团长订单
     */
    public PageResult<OrderVO> getLeaderOrders(Long leaderId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderMain> orderPage = orderMainRepository.findByLeaderIdOrderByCreateTimeDesc(leaderId, pageable);

        List<OrderVO> voList = orderPage.getContent().stream().map(this::convertToOrderVO)
            .collect(Collectors.toList());

        return PageResult.of(page, size, orderPage.getTotalElements(), voList);
    }

    /**
     * 管理端：导出订单
     */
    public void exportOrders(Integer status, Integer payStatus, String startDate, 
                           String endDate, jakarta.servlet.http.HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("订单数据_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")), 
                "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 查询数据（这里简化处理，实际应该根据条件查询）
            List<OrderMain> orders;
            if (status != null) {
                orders = orderMainRepository.findByOrderStatus(status);
            } else {
                orders = orderMainRepository.findAll();
            }

            // 构建Excel数据（这里简化，实际应该使用EasyExcel或POI）
            response.getWriter().write("订单ID,订单编号,用户ID,总金额,订单状态,创建时间\n");
            for (OrderMain order : orders) {
                response.getWriter().write(String.format("%d,%s,%d,%.2f,%d,%s\n",
                    order.getOrderId(),
                    order.getOrderSn(),
                    order.getUserId(),
                    order.getPayAmount(),
                    order.getOrderStatus(),
                    order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ));
            }
            response.getWriter().flush();
            
            log.info("导出订单成功: 共{}条数据", orders.size());
        } catch (Exception e) {
            log.error("导出订单失败", e);
            throw new BusinessException("导出订单失败: " + e.getMessage());
        }
    }

    /**
     * 转换OrderMain为OrderVO
     */
    private OrderVO convertToOrderVO(OrderMain order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderStatusText(OrderStatus.fromCode(order.getOrderStatus()).getDescription());

        // 查询订单商品
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
        vo.setItems(items.stream().map(this::convertToItemVO).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 生成订单编号
     */
    private String generateOrderSn() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return dateStr + random;
    }

    /**
     * 判断是否拼团订单（供PaymentService调用）⭐核心方法
     * 
     * <p>判断逻辑：订单明细中任一商品有activity_id，则为拼团订单
     * 
     * @param orderId 订单ID
     * @return true-拼团订单；false-普通订单
     */
    public Boolean isGroupBuyOrder(Long orderId) {
        log.info("判断是否拼团订单: orderId={}", orderId);

        // 查询订单明细
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        if (items.isEmpty()) {
            log.warn("订单无商品明细: orderId={}", orderId);
            return false;
        }

        // 如果任一商品有activity_id，则为拼团订单
        boolean isGroupBuy = items.stream()
            .anyMatch(item -> item.getActivityId() != null);

        log.info("判断结果: orderId={}, isGroupBuy={}", orderId, isGroupBuy);
        return isGroupBuy;
    }

    /**
     * 获取订单商品总数量
     *
     * @param orderId 订单ID
     * @return 商品总数量
     */
    public Integer getProductQuantity(Long orderId) {
        log.info("获取订单商品总数量: orderId={}", orderId);

        // 查询订单的所有商品项
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // 计算总数量
        Integer totalQuantity = items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();

        log.info("订单{}商品总数量: {}", orderId, totalQuantity);
        return totalQuantity;
    }

    /**
     * 转换订单项为VO
     */
    private OrderItemVO convertToItemVO(OrderItem item) {
        OrderItemVO vo = new OrderItemVO();
        BeanUtils.copyProperties(item, vo);
        return vo;
    }
}

