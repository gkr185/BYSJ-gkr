package com.bcu.edu.service;

import com.bcu.edu.client.GroupBuyServiceClient;
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
import java.util.ArrayList;
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

    @Autowired
    private GroupBuyServiceClient groupBuyServiceClient;

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

        // 保存旧状态用于判断是否需要生成佣金
        Integer oldStatus = order.getOrderStatus();
        
        order.setOrderStatus(newStatus);
        order.setUpdateTime(LocalDateTime.now());

        orderMainRepository.save(order);
        log.info("订单状态已更新: orderId={}, status={}", orderId, newStatus);

        // ⭐ 确认收货时生成佣金记录
        if (newStatus == 3 && !oldStatus.equals(3)) {
            generateCommissionForOrder(order);
        }
    }

    /**
     * 为订单生成佣金记录（⭐核心业务逻辑）
     */
    private void generateCommissionForOrder(OrderMain order) {
        try {
            // 参数验证
            if (order.getLeaderId() == null) {
                log.warn("订单leaderId为空，跳过佣金生成: orderId={}", order.getOrderId());
                return;
            }
            
            if (order.getPayAmount() == null || order.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("订单支付金额无效，跳过佣金生成: orderId={}, payAmount={}", 
                        order.getOrderId(), order.getPayAmount());
                return;
            }

            log.info("开始生成佣金记录: orderId={}, leaderId={}, payAmount={}", 
                    order.getOrderId(), order.getLeaderId(), order.getPayAmount());

            // 调用LeaderService生成佣金记录
            Result<Object> result = leaderServiceClient.generateCommission(
                order.getLeaderId(), 
                order.getOrderId(), 
                order.getPayAmount()
            );

            if (result.isSuccess()) {
                log.info("佣金记录生成成功: orderId={}, leaderId={}", 
                        order.getOrderId(), order.getLeaderId());
            } else {
                log.error("佣金记录生成失败: orderId={}, error={}", 
                        order.getOrderId(), result.getMessage());
            }
        } catch (Exception e) {
            log.error("佣金记录生成异常: orderId={}", order.getOrderId(), e);
        }
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

        // 填充扁平化字段（用于列表展示）
        if (!items.isEmpty()) {
            OrderItem firstItem = items.get(0);
            vo.setProductName(firstItem.getProductName());
            vo.setProductImg(firstItem.getProductImg());
            vo.setQuantity(firstItem.getQuantity());
        }

        // 填充用户名（简化显示）
        vo.setUserName("用户#" + order.getUserId());

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

    // ========== 团长订单查询接口 ==========

    /**
     * 查询团长订单列表（分页）
     * 
     * @param leaderId 团长ID
     * @param page 页码
     * @param size 每页数量
     * @param orderStatus 订单状态（可选）
     * @return 分页结果
     */
    public PageResult<OrderVO> getLeaderOrders(Long leaderId, Integer page, Integer size, Integer orderStatus) {
        log.info("查询团长订单: leaderId={}, page={}, size={}, status={}", leaderId, page, size, orderStatus);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderMain> orderPage;
        
        if (orderStatus != null) {
            // 按状态筛选
            orderPage = orderMainRepository.findByLeaderIdAndOrderStatusOrderByCreateTimeDesc(
                leaderId, orderStatus, pageable);
        } else {
            // 查询全部
            orderPage = orderMainRepository.findByLeaderIdOrderByCreateTimeDesc(leaderId, pageable);
        }
        
        // 转换为VO
        List<OrderVO> orderVOs = orderPage.getContent().stream()
            .map(this::convertToOrderVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(orderPage.getTotalElements(), orderVOs);
    }

    /**
     * 查询团长订单统计
     * 
     * @param leaderId 团长ID
     * @return 统计数据
     */
    public java.util.Map<String, Object> getLeaderOrdersSummary(Long leaderId) {
        log.info("查询团长订单统计: leaderId={}", leaderId);
        
        // 统计订单总数
        long totalCount = orderMainRepository.countByLeaderId(leaderId);
        
        // 统计今日订单数量
        LocalDateTime todayStart = LocalDateTime.now()
            .withHour(0).withMinute(0).withSecond(0);
        long todayCount = orderMainRepository
            .countByLeaderIdAndCreateTimeGreaterThanEqual(leaderId, todayStart);
        
        // 统计待发货订单数（订单状态 1-待发货）
        long pendingCount = orderMainRepository
            .countByLeaderIdAndOrderStatus(leaderId, 1);
        
        // 统计配送中订单数（订单状态 2-配送中）
        long deliveringCount = orderMainRepository
            .countByLeaderIdAndOrderStatus(leaderId, 2);
        
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("totalCount", totalCount);
        summary.put("todayCount", todayCount);
        summary.put("pendingCount", pendingCount);
        summary.put("deliveringCount", deliveringCount);
        
        log.info("团长{}订单统计: 总数={}, 今日={}, 待发货={}, 配送中={}", 
            leaderId, totalCount, todayCount, pendingCount, deliveringCount);
        
        return summary;
    }

    /**
     * 查询团订单列表（⭐核心方法）
     * 
     * <p>通过参团记录表建立团-订单关联关系，解决按团精确展示订单的问题
     * 
     * @param teamId 团ID
     * @return 订单列表
     */
    public List<OrderVO> getTeamOrders(Long teamId) {
        log.info("查询团订单: teamId={}", teamId);
        
        try {
            // 通过Feign调用GroupBuyService获取团的订单ID列表
            Result<List<Long>> result = groupBuyServiceClient.getTeamOrderIds(teamId);
            if (!result.isSuccess() || result.getData() == null) {
                log.warn("获取团订单ID列表失败: teamId={}, result={}", teamId, result);
                return new ArrayList<>();
            }
            
            List<Long> orderIds = result.getData();
            if (orderIds.isEmpty()) {
                log.info("团{}暂无订单", teamId);
                return new ArrayList<>();
            }
            
            // 根据订单ID列表查询订单详情
            List<OrderMain> orders = orderMainRepository.findByOrderIdIn(orderIds);
            
            // 转换为VO
            List<OrderVO> orderVOs = orders.stream()
                .map(this::convertToOrderVO)
                .collect(Collectors.toList());
            
            log.info("团{}查询到{}个订单", teamId, orderVOs.size());
            return orderVOs;
            
        } catch (Exception e) {
            log.error("查询团订单失败: teamId={}", teamId, e);
            return new ArrayList<>();
        }
    }
}

