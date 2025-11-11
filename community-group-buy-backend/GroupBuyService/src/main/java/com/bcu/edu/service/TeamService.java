package com.bcu.edu.service;

import com.bcu.edu.client.LeaderServiceClient;
import com.bcu.edu.client.OrderServiceClient;
import com.bcu.edu.client.PaymentServiceClient;
import com.bcu.edu.client.ProductServiceClient;
import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.CreateOrderRequest;
import com.bcu.edu.dto.request.CreatePaymentRequest;
import com.bcu.edu.dto.request.JoinTeamRequest;
import com.bcu.edu.dto.request.LaunchTeamRequest;
import com.bcu.edu.dto.response.*;
import com.bcu.edu.entity.GroupBuy;
import com.bcu.edu.entity.GroupBuyMember;
import com.bcu.edu.entity.GroupBuyTeam;
import com.bcu.edu.enums.MemberStatus;
import com.bcu.edu.enums.TeamStatus;
import com.bcu.edu.repository.GroupBuyRepository;
import com.bcu.edu.repository.MemberRepository;
import com.bcu.edu.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 团管理服务（⭐⭐⭐核心Service）
 * 
 * <p>核心功能：
 * <ul>
 *   <li>团长发起拼团（v3.0：验证团长身份、自动关联社区）</li>
 *   <li>用户参团（行锁防并发、防重复参团）</li>
 *   <li>支付回调（幂等性保证、成团检查）</li>
 *   <li>成团逻辑（双重幂等、批量更新）</li>
 *   <li>查询功能（社区优先推荐）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Service
@Slf4j
public class TeamService {
    
    @Autowired
    @Lazy  // ⭐使用@Lazy避免循环依赖
    private TeamService self;  // ⭐自注入，用于解决事务边界问题
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private GroupBuyRepository activityRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private OrderServiceClient orderServiceClient;
    
    @Autowired
    private ProductServiceClient productServiceClient;
    
    @Autowired
    private LeaderServiceClient leaderServiceClient;
    
    @Autowired
    private PaymentServiceClient paymentServiceClient;
    
    /**
     * 团长发起拼团（⭐v3.0核心功能）
     * 
     * <p>流程：
     * <ol>
     *   <li>检查活动有效性</li>
     *   <li>Feign调用UserService验证团长身份（role=2）</li>
     *   <li>获取团长的社区ID（v3.0自动关联）</li>
     *   <li>创建团实例（launcher_id = leader_id, community_id = 团长社区）</li>
     *   <li>生成团号（T + yyyyMMdd + 6位随机数）</li>
     *   <li>如果joinImmediately=true：创建订单+记录参团</li>
     * </ol>
     * 
     * @param request 发起请求
     * @return 团详情
     */
    @OperationLog(value = "发起拼团", module = "拼团管理")
    public TeamDetailResponse launchTeam(LaunchTeamRequest request) {
        // 1. 查询活动和验证用户（不需要事务）
        GroupBuy activity = activityRepository.findById(request.getActivityId())
            .orElseThrow(() -> new BusinessException("拼团活动不存在"));
        
        if (activity.getStatus() != 1) {
            throw new BusinessException("拼团活动未开始或已结束");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException("拼团活动未在活动时间范围内");
        }
        
        Result<UserInfoDTO> userResult = userServiceClient.getUserInfo(request.getUserId());
        if (userResult.getCode() != 200 || userResult.getData() == null) {
            throw new BusinessException("获取用户信息失败");
        }
        
        UserInfoDTO user = userResult.getData();
        if (user.getRole() == null || user.getRole() != 2) {
            throw new BusinessException("仅团长可发起拼团，当前角色：" + user.getRole());
        }
        
        log.info("用户{}（role={}）发起拼团，社区ID={}", user.getUserId(), user.getRole(), user.getCommunityId());
        
        // 2. ⭐关键：通过self调用事务方法，确保事务在此方法结束时提交
        LaunchTeamInternalResult internalResult = self.launchTeamInternal(request, activity, user);
        
        // 3. ⭐事务已提交，锁已释放，现在可以安全地调用 PaymentService
        if (Boolean.TRUE.equals(request.getJoinImmediately()) && internalResult.getOrderId() != null) {
            java.math.BigDecimal payAmount = activity.getGroupPrice().multiply(
                java.math.BigDecimal.valueOf(request.getQuantity()));
            
            CreatePaymentRequest paymentReq = CreatePaymentRequest.builder()
                .orderId(internalResult.getOrderId())
                .payType(3)  // 3-余额支付
                .amount(payAmount)
                .userId(request.getUserId())
                .build();
            
            Result<PaymentResponse> paymentResult = paymentServiceClient.createPayment(
                request.getUserId(),
                paymentReq
            );
            if (paymentResult.getCode() != 200) {
                throw new BusinessException("支付失败：" + paymentResult.getMessage());
            }
            
            PaymentResponse paymentResponse = paymentResult.getData();
            if (paymentResponse.getPayStatus() != 1) {
                throw new BusinessException("支付失败：" + 
                    (paymentResponse.getErrorMessage() != null ? paymentResponse.getErrorMessage() : "支付未完成"));
            }
            
            log.info("团长支付成功，payId={}, orderId={}", paymentResponse.getPayId(), internalResult.getOrderId());
            // ⭐PaymentService 成功后会自动回调 paymentCallback 更新团状态
        }
        
        // 4. 构建返回结果
        return buildTeamDetailResponse(internalResult.getTeam(), activity, user, null);
    }
    
    /**
     * 创建团和成员记录（独立事务）
     * 
     * <p>⭐关键设计：使用独立事务 (REQUIRES_NEW)
     * <p>确保在调用 PaymentService 之前，此方法的事务已经提交，锁已经释放
     * <p>避免 PaymentService 回调时出现死锁
     * 
     * @param request 发起请求
     * @param activity 活动信息
     * @param user 用户信息
     * @return 内部结果（包含团和订单ID）
     */
    @Transactional(rollbackFor = Exception.class)
    protected LaunchTeamInternalResult launchTeamInternal(LaunchTeamRequest request, 
                                                          GroupBuy activity, 
                                                          UserInfoDTO user) {
        // 1. 创建团实例
        GroupBuyTeam team = new GroupBuyTeam();
        team.setTeamNo(generateTeamNo());
        team.setActivityId(request.getActivityId());
        team.setLauncherId(request.getUserId());
        team.setLeaderId(request.getUserId());
        team.setCommunityId(user.getCommunityId());
        team.setRequiredNum(activity.getRequiredNum());
        team.setCurrentNum(0);
        team.setTeamStatus(TeamStatus.JOINING.getCode());
        team.setExpireTime(LocalDateTime.now().plusHours(request.getDurationHours()));
        teamRepository.save(team);
        
        log.info("团{}创建成功，teamNo={}, communityId={}", team.getTeamId(), team.getTeamNo(), team.getCommunityId());
        
        Long orderId = null;
        
        // 2. 如果团长立即参与，创建订单和成员记录
        if (Boolean.TRUE.equals(request.getJoinImmediately())) {
            if (request.getAddressId() == null) {
                throw new BusinessException("参与拼团需要提供收货地址");
            }
            
            // 获取商品信息
            Result<ProductDTO> productResult = productServiceClient.getProduct(activity.getProductId());
            ProductDTO product = productResult.getData();
            
            // Feign创建订单
            CreateOrderRequest orderReq = CreateOrderRequest.builder()
                .userId(request.getUserId())
                .leaderId(request.getUserId())
                .addressId(request.getAddressId())
                .productId(activity.getProductId())
                .productName(product.getProductName())
                .productImg(product.getCoverImg())
                .quantity(request.getQuantity())
                .price(activity.getGroupPrice())
                .activityId(activity.getActivityId())
                .build();
            
            Result<Long> orderResult = orderServiceClient.createOrder(orderReq);
            if (orderResult.getCode() != 200) {
                throw new BusinessException("创建订单失败：" + orderResult.getMessage());
            }
            
            orderId = orderResult.getData();
            log.info("团长参与拼团，订单创建成功，orderId={}", orderId);
            
            // 计算支付金额
            java.math.BigDecimal payAmount = activity.getGroupPrice().multiply(
                java.math.BigDecimal.valueOf(request.getQuantity()));
            
            // 记录参团（发起人）
            GroupBuyMember member = new GroupBuyMember();
            member.setTeamId(team.getTeamId());
            member.setUserId(request.getUserId());
            member.setOrderId(orderId);
            member.setIsLauncher(1);
            member.setProductQuantity(request.getQuantity());
            member.setPayAmount(payAmount);
            member.setStatus(MemberStatus.UNPAID.getCode());
            memberRepository.save(member);
            
            log.info("团长参团记录创建成功，memberId={}, status=UNPAID", member.getMemberId());
        }
        
        return new LaunchTeamInternalResult(team, orderId);
    }
    
    /**
     * 内部结果DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    protected static class LaunchTeamInternalResult {
        private GroupBuyTeam team;
        private Long orderId;
    }
    
    /**
     * 用户参与拼团（⭐核心功能）
     * 
     * <p>流程：
     * <ol>
     *   <li>查询团（加行锁）⭐</li>
     *   <li>状态校验（团状态、人数、过期）</li>
     *   <li>防重复参团检查（唯一索引）</li>
     *   <li>Feign创建订单</li>
     *   <li>记录参团（status=0待支付）</li>
     * </ol>
     * 
     * @param request 参团请求
     * @return 参团结果
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "参与拼团", module = "拼团管理")
    public JoinResult joinTeam(JoinTeamRequest request) {
        // 1. 查询团（加行锁）⭐
        GroupBuyTeam team = teamRepository.findByIdForUpdate(request.getTeamId())
            .orElseThrow(() -> new BusinessException("团不存在"));
        
        log.info("用户{}参团，teamId={}, 当前人数={}/{}", request.getUserId(), team.getTeamId(), 
            team.getCurrentNum(), team.getRequiredNum());
        
        // 2. 状态校验
        if (team.getTeamStatus() != TeamStatus.JOINING.getCode()) {
            throw new BusinessException("拼团已结束，当前状态：" + TeamStatus.getByCode(team.getTeamStatus()).getDesc());
        }
        
        if (team.getCurrentNum() >= team.getRequiredNum()) {
            throw new BusinessException("团已满员");
        }
        
        if (team.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("拼团已过期");
        }
        
        // 3. 防重复参团
        if (memberRepository.existsByTeamIdAndUserId(request.getTeamId(), request.getUserId())) {
            throw new BusinessException("您已参加此团");
        }
        
        // 4. 查询活动和商品信息
        GroupBuy activity = activityRepository.findById(team.getActivityId())
            .orElseThrow(() -> new BusinessException("活动不存在"));
        
        Result<ProductDTO> productResult = productServiceClient.getProduct(activity.getProductId());
        if (productResult.getCode() != 200 || productResult.getData() == null) {
            throw new BusinessException("获取商品信息失败");
        }
        ProductDTO product = productResult.getData();
        
        // 5. Feign创建订单 ⭐
        CreateOrderRequest orderReq = CreateOrderRequest.builder()
            .userId(request.getUserId())
            .leaderId(team.getLeaderId())
            .addressId(request.getAddressId())
            .productId(activity.getProductId())
            .productName(product.getProductName())
            .productImg(product.getCoverImg())
            .quantity(request.getQuantity())
            .price(activity.getGroupPrice())
            .activityId(activity.getActivityId())
            .build();
        
        Result<Long> orderResult = orderServiceClient.createOrder(orderReq);
        if (orderResult.getCode() != 200) {
            throw new BusinessException("创建订单失败：" + orderResult.getMessage());
        }
        
        Long orderId = orderResult.getData();
        log.info("参团订单创建成功，orderId={}", orderId);
        
        // 6. 记录参团
        GroupBuyMember member = new GroupBuyMember();
        member.setTeamId(request.getTeamId());
        member.setUserId(request.getUserId());
        member.setOrderId(orderId);
        member.setIsLauncher(0);
        member.setProductQuantity(request.getQuantity());
        member.setPayAmount(activity.getGroupPrice().multiply(java.math.BigDecimal.valueOf(request.getQuantity())));
        member.setStatus(MemberStatus.UNPAID.getCode());
        memberRepository.save(member);
        
        log.info("参团记录创建成功，memberId={}", member.getMemberId());
        
        // 7. 构建返回结果
        return JoinResult.builder()
            .orderId(orderId)
            .teamId(team.getTeamId())
            .teamNo(team.getTeamNo())
            .currentNum(team.getCurrentNum())
            .requiredNum(team.getRequiredNum())
            .remainNum(team.getRequiredNum() - team.getCurrentNum())
            .payAmount(member.getPayAmount())
            .expireTime(team.getExpireTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .build();
    }
    
    /**
     * 支付回调处理（⭐核心功能）
     * 
     * <p>流程：
     * <ol>
     *   <li>查询参团记录（加行锁）⭐</li>
     *   <li>幂等性检查（status != UNPAID 则返回）⭐</li>
     *   <li>更新参团状态（UNPAID → PAID）</li>
     *   <li>查询团（加行锁）⭐</li>
     *   <li>更新团人数（current_num++）</li>
     *   <li>检查是否成团（current_num >= required_num）</li>
     *   <li>如果成团，调用teamSuccess</li>
     * </ol>
     * 
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void paymentCallback(Long orderId) {
        // 1. 查询参团记录（加行锁）⭐
        GroupBuyMember member = memberRepository.findByOrderIdForUpdate(orderId)
            .orElseThrow(() -> new BusinessException("参团记录不存在"));
        
        log.info("支付回调，orderId={}, 当前状态={}", orderId, member.getStatus());
        
        // 2. 幂等性检查 ⭐
        if (member.getStatus() != MemberStatus.UNPAID.getCode()) {
            log.warn("重复支付回调，orderId={}, status={}", orderId, member.getStatus());
            return;  // 已处理过，直接返回
        }
        
        // 3. 更新参团状态
        member.setStatus(MemberStatus.PAID.getCode());
        memberRepository.save(member);
        log.info("参团状态更新为已支付，memberId={}", member.getMemberId());
        
        // 4. 查询团（加行锁）⭐
        GroupBuyTeam team = teamRepository.findByIdForUpdate(member.getTeamId())
            .orElseThrow(() -> new BusinessException("团不存在"));
        
        // 5. 更新团人数
        team.setCurrentNum(team.getCurrentNum() + 1);
        teamRepository.save(team);
        log.info("团人数更新，teamId={}, currentNum={}/{}", team.getTeamId(), 
            team.getCurrentNum(), team.getRequiredNum());
        
        // 6. 检查是否成团
        if (team.getCurrentNum() >= team.getRequiredNum()) {
            log.info("团{}已满足成团条件，触发成团逻辑", team.getTeamId());
            teamSuccess(team.getTeamId());
        }
    }
    
    /**
     * 成团逻辑（⭐核心功能）
     * 
     * <p>流程：
     * <ol>
     *   <li>查询团（加行锁）⭐</li>
     *   <li>幂等性检查（team_status != JOINING 则返回）⭐</li>
     *   <li>更新团状态（JOINING → SUCCESS）</li>
     *   <li>查询所有成员</li>
     *   <li>批量更新成员状态（PAID → SUCCESS）</li>
     *   <li>Feign批量更新订单状态（TO_DELIVER）</li>
     *   <li>发送成团通知（异步）</li>
     * </ol>
     * 
     * @param teamId 团ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void teamSuccess(Long teamId) {
        // 1. 查询团（加行锁）⭐
        GroupBuyTeam team = teamRepository.findByIdForUpdate(teamId)
            .orElseThrow(() -> new BusinessException("团不存在"));
        
        // 2. 幂等性检查 ⭐
        if (team.getTeamStatus() != TeamStatus.JOINING.getCode()) {
            log.warn("重复成团，teamId={}, status={}", teamId, team.getTeamStatus());
            return;  // 已处理过，直接返回
        }
        
        log.info("开始执行成团逻辑，teamId={}", teamId);
        
        // 3. 更新团状态
        team.setTeamStatus(TeamStatus.SUCCESS.getCode());
        team.setSuccessTime(LocalDateTime.now());
        teamRepository.save(team);
        log.info("团状态更新为已成团，teamId={}", teamId);
        
        // 4. 查询所有成员
        List<GroupBuyMember> members = memberRepository.findByTeamIdOrderByJoinTimeAsc(teamId);
        log.info("团{}共有{}个成员", teamId, members.size());
        
        // 5. 批量更新成员状态
        members.forEach(m -> m.setStatus(MemberStatus.SUCCESS.getCode()));
        memberRepository.saveAll(members);
        log.info("所有成员状态更新为已成团");
        
        // 6. 批量更新订单状态（Feign）⭐
        List<Long> orderIds = members.stream()
            .map(GroupBuyMember::getOrderId)
            .collect(Collectors.toList());
        
        try {
            Result<Void> result = orderServiceClient.batchUpdateOrderStatus(orderIds, 1);  // 1-待发货
            if (result.getCode() == 200) {
                log.info("批量更新订单状态成功，orderIds={}", orderIds);
            } else {
                log.error("批量更新订单状态失败，orderIds={}, msg={}", orderIds, result.getMessage());
                // 不抛异常，后续可通过补偿任务修复
            }
        } catch (Exception e) {
            log.error("批量更新订单状态异常，orderIds={}", orderIds, e);
            // 不抛异常，成团逻辑已完成
        }
        
        // 7. 发送通知（TODO: 异步）
        log.info("成团通知发送（TODO）");
    }
    
    /**
     * 获取团详情
     */
    public TeamDetailResponse getTeamDetail(Long teamId) {
        GroupBuyTeam team = teamRepository.findById(teamId)
            .orElseThrow(() -> new BusinessException("团不存在"));
        
        GroupBuy activity = activityRepository.findById(team.getActivityId())
            .orElseThrow(() -> new BusinessException("活动不存在"));
        
        // 获取团长信息
        Result<UserInfoDTO> userResult = userServiceClient.getUserInfo(team.getLeaderId());
        UserInfoDTO leader = userResult.getCode() == 200 ? userResult.getData() : null;
        
        // 获取社区信息
        CommunityDTO community = null;
        if (team.getCommunityId() != null) {
            try {
                Result<CommunityDTO> communityResult = leaderServiceClient.getCommunity(team.getCommunityId());
                community = communityResult.getCode() == 200 ? communityResult.getData() : null;
            } catch (Exception e) {
                log.warn("获取社区信息失败，communityId={}", team.getCommunityId(), e);
            }
        }
        
        // 获取成员列表
        List<GroupBuyMember> members = memberRepository.findByTeamIdOrderByJoinTimeAsc(teamId);
        List<MemberInfoResponse> memberInfos = members.stream()
            .map(this::convertToMemberInfo)
            .collect(Collectors.toList());
        
        return buildTeamDetailResponse(team, activity, leader, community, memberInfos);
    }
    
    /**
     * 获取活动的团列表（社区优先排序，支持状态筛选）⭐v3.0优化
     * 
     * @param activityId 活动ID
     * @param communityId 用户社区ID（可选）
     * @param status 团状态（null表示默认只显示拼团中）
     * @param includeExpired 是否包含已过期的团（默认false）
     * @return 团列表
     */
    public List<TeamDetailResponse> getActivityTeams(Long activityId, Long communityId, 
                                                     Integer status, Boolean includeExpired) {
        List<GroupBuyTeam> teams;
        
        // 如果没有指定status且includeExpired=false，使用原有的优化查询（C端用）
        // 否则使用新的筛选查询（管理端用）
        if (status == null && !Boolean.TRUE.equals(includeExpired)) {
            // C端：默认只显示拼团中且未过期的团
            teams = teamRepository.findByActivityIdWithCommunityPriority(
                activityId,
                communityId != null ? communityId : 0L,
                20
            );
        } else {
            // 管理端：支持状态筛选和查看已过期团
            // 如果status为null，默认显示拼团中(0)的团
            Integer queryStatus = status != null ? status : 0;
            teams = teamRepository.findByActivityIdWithFilters(
                activityId,
                communityId != null ? communityId : 0L,
                status,  // null表示不限制状态
                Boolean.TRUE.equals(includeExpired),
                50  // 管理端可以显示更多
            );
        }
        
        GroupBuy activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new BusinessException("活动不存在"));
        
        return teams.stream()
            .map(team -> buildTeamDetailResponse(team, activity, null, null))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取活动的团列表（旧版本，保持向后兼容）
     * 
     * @deprecated 请使用 {@link #getActivityTeams(Long, Long, Integer, Boolean)}
     */
    @Deprecated
    public List<TeamDetailResponse> getActivityTeams(Long activityId, Long communityId) {
        return getActivityTeams(activityId, communityId, null, false);
    }
    
    /**
     * 生成团号（格式：T20251031001）
     */
    private String generateTeamNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "T" + date + random;
    }
    
    /**
     * 构建团详情响应
     */
    private TeamDetailResponse buildTeamDetailResponse(GroupBuyTeam team, GroupBuy activity, 
                                                       UserInfoDTO leader, CommunityDTO community) {
        return buildTeamDetailResponse(team, activity, leader, community, null);
    }
    
    private TeamDetailResponse buildTeamDetailResponse(GroupBuyTeam team, GroupBuy activity, 
                                                       UserInfoDTO leader, CommunityDTO community,
                                                       List<MemberInfoResponse> members) {
        // 获取商品信息
        ProductDTO product = null;
        try {
            Result<ProductDTO> productResult = productServiceClient.getProduct(activity.getProductId());
            if (productResult != null && productResult.getCode() == 200) {
                product = productResult.getData();
            }
        } catch (Exception e) {
            log.warn("获取商品{}信息失败: {}", activity.getProductId(), e.getMessage());
        }
        
        return TeamDetailResponse.builder()
            .teamId(team.getTeamId())
            .teamNo(team.getTeamNo())
            .activityId(team.getActivityId())
            .productId(activity.getProductId())
            .productName(product != null ? product.getProductName() : "商品ID-" + activity.getProductId())
            .productCoverImg(product != null ? product.getCoverImg() : null)
            .productPrice(product != null ? product.getPrice() : null)
            .activityName(product != null ? product.getProductName() : "拼团活动-" + activity.getProductId())
            .groupPrice(activity.getGroupPrice())
            .leaderId(team.getLeaderId())
            .leaderName(leader != null ? leader.getRealName() : null)
            .communityId(team.getCommunityId())
            .communityName(community != null ? community.getCommunityName() : null)
            .requiredNum(team.getRequiredNum())
            .currentNum(team.getCurrentNum())
            .remainNum(team.getRequiredNum() - team.getCurrentNum())
            .teamStatus(team.getTeamStatus())
            .teamStatusDesc(TeamStatus.getByCode(team.getTeamStatus()).getDesc())
            .successTime(team.getSuccessTime())
            .expireTime(team.getExpireTime())
            .createTime(team.getCreateTime())
            .members(members)
            .shareLink("http://localhost:5173/team/" + team.getTeamId())
            .build();
    }
    
    /**
     * 获取团长发起的拼团记录（带分页、状态筛选）
     * 
     * <p>返回团长发起的所有拼团，按创建时间倒序
     * 
     * @param leaderId 团长ID
     * @param status 团状态（null表示全部）
     * @param page 页码（从1开始）
     * @param limit 每页数量
     * @return 分页结果 {list, total}
     */
    public com.bcu.edu.common.result.PageResult<TeamDetailResponse> getLeaderTeams(
        Long leaderId, Integer status, int page, int limit) {
        
        log.info("查询团长{}的拼团记录，status={}, page={}, limit={}", leaderId, status, page, limit);
        
        // 计算偏移量（page是0-indexed，所以直接用page*limit）
        int offset = page * limit;
        
        // 查询团列表
        List<GroupBuyTeam> teams = teamRepository.findByLeaderIdWithFilter(leaderId, status, limit, offset);
        
        // 统计总数
        long total = teamRepository.countByLeaderIdAndStatus(leaderId, status);
        
        log.info("团长{}共有{}个拼团记录，本页返回{}个", leaderId, total, teams.size());
        
        // 构建团详情列表
        List<TeamDetailResponse> teamDetails = teams.stream()
            .map(team -> {
                try {
                    // 查询活动信息
                    GroupBuy activity = activityRepository.findById(team.getActivityId()).orElse(null);
                    if (activity == null) {
                        return null;
                    }
                    
                    // 查询团长信息
                    UserInfoDTO leader = null;
                    try {
                        Result<UserInfoDTO> leaderResult = userServiceClient.getUserInfo(team.getLeaderId());
                        if (leaderResult != null && leaderResult.getCode() == 200) {
                            leader = leaderResult.getData();
                        }
                    } catch (Exception e) {
                        log.warn("获取团长{}信息失败: {}", team.getLeaderId(), e.getMessage());
                    }
                    
                    // 查询社区信息
                    CommunityDTO community = null;
                    if (team.getCommunityId() != null) {
                        try {
                            Result<CommunityDTO> communityResult = leaderServiceClient.getCommunity(team.getCommunityId());
                            if (communityResult != null && communityResult.getCode() == 200) {
                                community = communityResult.getData();
                            }
                        } catch (Exception e) {
                            log.warn("获取社区{}信息失败: {}", team.getCommunityId(), e.getMessage());
                        }
                    }
                    
                    // 查询团的所有成员
                    List<GroupBuyMember> teamMembers = memberRepository.findByTeamIdOrderByJoinTimeAsc(team.getTeamId());
                    List<MemberInfoResponse> memberInfos = teamMembers.stream()
                        .map(this::convertToMemberInfo)
                        .collect(Collectors.toList());
                    
                    return buildTeamDetailResponse(team, activity, leader, community, memberInfos);
                } catch (Exception e) {
                    log.error("构建拼团记录失败，teamId={}: {}", team.getTeamId(), e.getMessage(), e);
                    return null;
                }
            })
            .filter(team -> team != null)
            .collect(Collectors.toList());
        
        return com.bcu.edu.common.result.PageResult.of(page, limit, total, teamDetails);
    }
    
    /**
     * 获取用户参与的拼团记录
     * 
     * <p>返回用户参与的所有拼团，按参团时间倒序
     * 
     * @param userId 用户ID
     * @return 拼团记录列表
     */
    public List<MyTeamResponse> getUserTeams(Long userId) {
        log.info("查询用户{}的拼团记录", userId);
        
        // 查询用户的所有参团记录
        List<GroupBuyMember> members = memberRepository.findByUserIdOrderByJoinTimeDesc(userId);
        
        if (members.isEmpty()) {
            return List.of();
        }
        
        // 构建我的拼团列表
        return members.stream()
            .map(member -> {
                try {
                    // 查询团信息
                    GroupBuyTeam team = teamRepository.findById(member.getTeamId()).orElse(null);
                    if (team == null) {
                        return null;
                    }
                    
                    // 查询活动信息
                    GroupBuy activity = activityRepository.findById(team.getActivityId()).orElse(null);
                    if (activity == null) {
                        return null;
                    }
                    
                    // 查询团长信息
                    UserInfoDTO leader = null;
                    try {
                        Result<UserInfoDTO> leaderResult = userServiceClient.getUserInfo(team.getLeaderId());
                        if (leaderResult != null && leaderResult.getCode() == 200) {
                            leader = leaderResult.getData();
                        }
                    } catch (Exception e) {
                        log.warn("获取团长{}信息失败: {}", team.getLeaderId(), e.getMessage());
                    }
                    
                    // 查询社区信息
                    CommunityDTO community = null;
                    try {
                        Result<CommunityDTO> communityResult = leaderServiceClient.getCommunity(team.getCommunityId());
                        if (communityResult != null && communityResult.getCode() == 200) {
                            community = communityResult.getData();
                        }
                    } catch (Exception e) {
                        log.warn("获取社区{}信息失败: {}", team.getCommunityId(), e.getMessage());
                    }
                    
                    // 获取商品信息
                    ProductDTO product = null;
                    try {
                        Result<ProductDTO> productResult = productServiceClient.getProduct(activity.getProductId());
                        if (productResult != null && productResult.getCode() == 200) {
                            product = productResult.getData();
                        }
                    } catch (Exception e) {
                        log.warn("获取商品{}信息失败: {}", activity.getProductId(), e.getMessage());
                    }
                    
                    // 查询团的所有成员（用于详情对话框）
                    List<GroupBuyMember> teamMembers = memberRepository.findByTeamIdOrderByJoinTimeAsc(team.getTeamId());
                    List<MemberInfoResponse> memberInfos = teamMembers.stream()
                        .map(this::convertToMemberInfo)
                        .collect(Collectors.toList());
                    
                    // 构建我的拼团响应
                    return MyTeamResponse.builder()
                        // 参团记录信息
                        .memberId(member.getMemberId())
                        .isLauncher(member.getIsLauncher())
                        .payAmount(member.getPayAmount())
                        .joinTime(member.getJoinTime())
                        .orderId(member.getOrderId())
                        // 团信息
                        .teamId(team.getTeamId())
                        .teamNo(team.getTeamNo())
                        .activityId(team.getActivityId())
                        .activityName(product != null ? product.getProductName() : "拼团活动-" + activity.getProductId())
                        .productId(activity.getProductId())
                        .productName(product != null ? product.getProductName() : "商品ID-" + activity.getProductId())
                        .productCoverImg(product != null ? product.getCoverImg() : null)
                        .productPrice(product != null ? product.getPrice() : null)
                        .groupPrice(activity.getGroupPrice())
                        .leaderId(team.getLeaderId())
                        .leaderName(leader != null ? leader.getRealName() : null)
                        .communityId(team.getCommunityId())
                        .communityName(community != null ? community.getCommunityName() : null)
                        .requiredNum(team.getRequiredNum())
                        .currentNum(team.getCurrentNum())
                        .teamStatus(team.getTeamStatus())
                        .teamStatusDesc(TeamStatus.getByCode(team.getTeamStatus()).getDesc())
                        .successTime(team.getSuccessTime())
                        .expireTime(team.getExpireTime())
                        .createTime(team.getCreateTime())
                        .members(memberInfos)
                        .shareLink("http://localhost:5173/team/" + team.getTeamId())
                        .build();
                } catch (Exception e) {
                    log.error("构建拼团记录失败，memberId={}: {}", member.getMemberId(), e.getMessage(), e);
                    return null;
                }
            })
            .filter(team -> team != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 转换成员信息
     */
    private MemberInfoResponse convertToMemberInfo(GroupBuyMember member) {
        // 获取用户详细信息
        String username = "用户" + member.getUserId();
        String realName = null;
        String avatar = null;

        try {
            Result<UserInfoDTO> userResult = userServiceClient.getUserInfo(member.getUserId());
            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                UserInfoDTO user = userResult.getData();
                username = user.getUsername() != null ? user.getUsername() : username;
                realName = user.getRealName();
                avatar = user.getAvatar();
            }
        } catch (Exception e) {
            log.warn("获取用户{}信息失败: {}", member.getUserId(), e.getMessage());
        }

        // 获取商品数量（直接从数据库字段获取）
        Integer productQuantity = member.getProductQuantity() != null ? member.getProductQuantity() : 0;

        return MemberInfoResponse.builder()
            .memberId(member.getMemberId())
            .userId(member.getUserId())
            .username(username)
            .realName(realName)
            .avatar(avatar)
            .isLauncher(member.getIsLauncher())
            .payAmount(member.getPayAmount())
            .joinTime(member.getJoinTime())
            .status(member.getStatus())
            .statusDesc(MemberStatus.getByCode(member.getStatus()).getDesc())
            .productQuantity(productQuantity)
            .build();
    }
    
    // ==================== 团长管理团队方法 ====================
    
    /**
     * 团长提前结束拼团（已达起拼人数）
     * 
     * <p>流程：
     * <ol>
     *   <li>校验团长权限</li>
     *   <li>检查团状态为"拼团中"</li>
     *   <li>检查是否达到起拼人数</li>
     *   <li>调用成团逻辑</li>
     * </ol>
     * 
     * @param teamId 团ID
     * @param userId 用户ID（团长）
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "提前结束拼团", module = "拼团管理")
    public void finishTeamEarly(Long teamId, Long userId) {
        log.info("提前结束拼团，teamId={}, userId={}", teamId, userId);
        
        // 1. 查询团信息（加行锁）
        GroupBuyTeam team = teamRepository.findByIdForUpdate(teamId)
            .orElseThrow(() -> new BusinessException("拼团不存在"));
        
        // 2. 校验团长权限
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅团长可以提前结束拼团");
        }
        
        // 3. 检查团状态
        if (team.getTeamStatus() != TeamStatus.JOINING.getCode()) {
            throw new BusinessException("该拼团已结束，当前状态：" + TeamStatus.getByCode(team.getTeamStatus()).getDesc());
        }
        
        // 4. 检查是否达到起拼人数
        if (team.getCurrentNum() < team.getRequiredNum()) {
            throw new BusinessException("未达到起拼人数，当前" + team.getCurrentNum() + "人，需要" + team.getRequiredNum() + "人");
        }
        
        // 5. 调用成团逻辑
        log.info("团长提前结束拼团，触发成团，teamId={}", teamId);
        teamSuccess(teamId);
    }
    
    /**
     * 团长取消拼团
     * 
     * <p>流程：
     * <ol>
     *   <li>校验团长权限</li>
     *   <li>检查团状态为"拼团中"</li>
     *   <li>更新团状态为"已失败"</li>
     *   <li>更新所有成员状态为"已取消"</li>
     *   <li>批量退款给所有已支付成员</li>
     *   <li>批量更新订单状态为"已退款"</li>
     * </ol>
     * 
     * @param teamId 团ID
     * @param userId 用户ID（团长）
     * @param reason 取消原因
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "取消拼团", module = "拼团管理")
    public void cancelTeam(Long teamId, Long userId, String reason) {
        log.info("团长取消拼团，teamId={}, userId={}, reason={}", teamId, userId, reason);
        
        // 1. 查询团信息（加行锁）
        GroupBuyTeam team = teamRepository.findByIdForUpdate(teamId)
            .orElseThrow(() -> new BusinessException("拼团不存在"));
        
        // 2. 校验团长权限
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅团长可以取消拼团");
        }
        
        // 3. 检查团状态
        if (team.getTeamStatus() != TeamStatus.JOINING.getCode()) {
            throw new BusinessException("该拼团已结束，当前状态：" + TeamStatus.getByCode(team.getTeamStatus()).getDesc());
        }
        
        // 4. 更新团状态为"已失败"
        team.setTeamStatus(TeamStatus.FAILED.getCode());
        teamRepository.save(team);
        log.info("团状态已更新为失败，teamId={}", teamId);
        
        // 5. 查询所有成员
        List<GroupBuyMember> members = memberRepository.findByTeamIdOrderByJoinTimeAsc(teamId);
        
        // 6. 批量退款和更新订单状态（在更新状态之前）
        int refundCount = 0;
        for (GroupBuyMember member : members) {
            // 只退已支付或已成团状态的成员
            if (member.getStatus() == MemberStatus.PAID.getCode() || 
                member.getStatus() == MemberStatus.SUCCESS.getCode()) {
                
                try {
                    // 退款到用户余额
                    Result<Void> refundResult = userServiceClient.refundToBalance(
                        member.getUserId(), member.getPayAmount());
                    if (refundResult == null || refundResult.getCode() != 200) {
                        log.error("退款到余额失败，orderId={}, userId={}", member.getOrderId(), member.getUserId());
                    } else {
                        log.info("退款到余额成功，userId={}, amount={}", 
                            member.getUserId(), member.getPayAmount());
                        
                        // 更新订单状态为"已退款"
                        orderServiceClient.updateOrderStatus(member.getOrderId(), 6);
                        log.info("订单状态已更新为已退款，orderId={}", member.getOrderId());
                        refundCount++;
                    }
                } catch (Exception e) {
                    log.error("退款异常，orderId={}, userId={}: {}", 
                        member.getOrderId(), member.getUserId(), e.getMessage(), e);
                }
            }
        }
        
        // 7. 更新所有成员状态为"已取消"
        members.forEach(member -> {
            member.setStatus(MemberStatus.CANCELLED.getCode());
        });
        memberRepository.saveAll(members);
        log.info("团成员状态已更新为已取消，共{}人", members.size());
        
        log.info("拼团已取消并退款，teamId={}, 退款成员数={}", teamId, refundCount);
    }
    
    /**
     * 团长移除团队成员
     * 
     * <p>流程：
     * <ol>
     *   <li>校验团长权限</li>
     *   <li>检查团状态为"拼团中"</li>
     *   <li>检查被移除成员不是团长本人</li>
     *   <li>删除参团记录</li>
     *   <li>更新团人数</li>
     *   <li>退款给被移除成员</li>
     *   <li>更新订单状态为"已退款"</li>
     * </ol>
     * 
     * @param teamId 团ID
     * @param memberId 成员ID
     * @param userId 用户ID（团长）
     * @param reason 移除原因
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "移除团队成员", module = "拼团管理")
    public void removeMember(Long teamId, Long memberId, Long userId, String reason) {
        log.info("团长移除成员，teamId={}, memberId={}, userId={}, reason={}", 
            teamId, memberId, userId, reason);
        
        // 1. 查询团信息（加行锁）
        GroupBuyTeam team = teamRepository.findByIdForUpdate(teamId)
            .orElseThrow(() -> new BusinessException("拼团不存在"));
        
        // 2. 校验团长权限
        if (!team.getLeaderId().equals(userId)) {
            throw new BusinessException("仅团长可以移除成员");
        }
        
        // 3. 检查团状态
        if (team.getTeamStatus() != TeamStatus.JOINING.getCode()) {
            throw new BusinessException("该拼团已结束，当前状态：" + TeamStatus.getByCode(team.getTeamStatus()).getDesc());
        }
        
        // 4. 查询被移除成员
        GroupBuyMember member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException("成员不存在"));
        
        // 5. 检查成员是否属于该团
        if (!member.getTeamId().equals(teamId)) {
            throw new BusinessException("该成员不属于此团");
        }
        
        // 6. 检查不能移除团长本人
        if (member.getIsLauncher() == 1) {
            throw new BusinessException("不能移除团长本人");
        }
        
        // 7. 退款（如果成员已支付）⭐先退款再删除记录
        boolean needDecrementCount = false;
        if (member.getStatus() == MemberStatus.PAID.getCode() || 
            member.getStatus() == MemberStatus.SUCCESS.getCode()) {
            needDecrementCount = true;  // 已支付成员需要减少团人数
            
            try {
                // 调用PaymentService进行退款，会自动：
                // 1. 创建退款记录（payment_record表）
                // 2. 调用UserService更新用户余额
                // 3. 确保退款在支付管理中可见
                com.bcu.edu.dto.request.RefundRequest refundRequest = com.bcu.edu.dto.request.RefundRequest.builder()
                    .orderId(member.getOrderId())
                    .reason("团长移除成员：" + reason)
                    .build();
                
                Result<Void> refundResult = paymentServiceClient.refund(refundRequest);
                if (refundResult == null || refundResult.getCode() != 200) {
                    log.error("退款失败，orderId={}, userId={}, result={}", 
                        member.getOrderId(), member.getUserId(), refundResult);
                    throw new BusinessException("退款失败，请稍后重试");
                }
                
                log.info("移除成员并退款成功，orderId={}, userId={}, amount={}", 
                    member.getOrderId(), member.getUserId(), member.getPayAmount());
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("退款异常，orderId={}, userId={}: {}", 
                    member.getOrderId(), member.getUserId(), e.getMessage(), e);
                throw new BusinessException("退款失败：" + e.getMessage());
            }
        }
        
        // 8. 删除参团记录
        memberRepository.delete(member);
        log.info("参团记录已删除，memberId={}, status={}", memberId, member.getStatus());
        
        // 9. 更新团人数（⭐只有已支付成员才减少团人数）
        if (needDecrementCount && team.getCurrentNum() > 0) {
            team.setCurrentNum(team.getCurrentNum() - 1);
            teamRepository.save(team);
            log.info("团人数已更新（移除已支付成员），teamId={}, currentNum={}", teamId, team.getCurrentNum());
        } else if (!needDecrementCount) {
            log.info("未支付成员无需减少团人数，memberId={}, status={}", memberId, member.getStatus());
        } else {
            log.warn("团人数已为0，无需减少，teamId={}", teamId);
        }
        
        log.info("成员已移除，teamId={}, memberId={}, userId={}", teamId, memberId, member.getUserId());
    }
}

