package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.service.GroupBuyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * GroupBuyService Feign内部接口Controller（供其他微服务调用）
 *
 * <p>🔴 核心接口！供CartService调用
 *
 * @author 耿康瑞
 * @since 2025-11-04
 */
@RestController
@RequestMapping("/api/groupbuy/feign")
@Tag(name = "拼团Feign接口", description = "供其他微服务调用的内部接口")
@Slf4j
public class FeignController {

    @Autowired
    private GroupBuyService groupBuyService;

    /**
     * 验证拼团活动是否存在（⭐关键接口）
     *
     * <p>调用方: CartService.addToCart(), CartService.convertToCartItemVO()
     * <p>场景: 添加购物车时验证拼团活动有效性
     *
     * @param activityId 拼团活动ID
     * @return true-存在；false-不存在
     */
    @GetMapping("/validateActivity/{activityId}")
    @Operation(summary = "验证拼团活动是否存在", description = "供CartService调用，验证活动有效性")
    public Result<Boolean> validateActivity(@PathVariable("activityId") Long activityId) {
        log.info("Feign调用: 验证拼团活动, activityId={}", activityId);

        try {
            Boolean exists = groupBuyService.activityExists(activityId);
            log.info("验证结果: activityId={}, exists={}", activityId, exists);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("验证拼团活动失败", e);
            return Result.error("验证拼团活动失败: " + e.getMessage());
        }
    }

    /**
     * 获取拼团活动价格（⭐关键接口）
     *
     * <p>调用方: CartService.convertToCartItemVO()
     * <p>场景: 购物车显示时获取拼团价格
     *
     * @param activityId 拼团活动ID
     * @return 拼团价格
     */
    @GetMapping("/activityPrice/{activityId}")
    @Operation(summary = "获取拼团活动价格", description = "供CartService调用，获取活动价格")
    public Result<BigDecimal> getActivityPrice(@PathVariable("activityId") Long activityId) {
        log.info("Feign调用: 获取拼团活动价格, activityId={}", activityId);

        try {
            BigDecimal price = groupBuyService.getActivityPrice(activityId);
            log.info("获取价格结果: activityId={}, price={}", activityId, price);
            return Result.success(price);
        } catch (Exception e) {
            log.error("获取拼团活动价格失败", e);
            return Result.error("获取拼团活动价格失败: " + e.getMessage());
        }
    }

    /**
     * 获取团的订单ID列表（⭐团长订单管理核心接口）
     *
     * <p>调用方: OrderService.getTeamOrders()
     * <p>场景: 团长查看某个团的所有订单，通过参团记录表建立关联
     *
     * @param teamId 团ID
     * @return 订单ID列表
     */
    @GetMapping("/teamOrderIds/{teamId}")
    @Operation(summary = "获取团的订单ID列表", description = "供OrderService调用，获取团的所有订单ID")
    public Result<List<Long>> getTeamOrderIds(@PathVariable("teamId") Long teamId) {
        log.info("Feign调用: 获取团订单ID列表, teamId={}", teamId);

        try {
            List<Long> orderIds = groupBuyService.getTeamOrderIds(teamId);
            log.info("获取结果: teamId={}, orderIds={}", teamId, orderIds);
            return Result.success(orderIds);
        } catch (Exception e) {
            log.error("获取团订单ID列表失败", e);
            return Result.error("获取团订单ID列表失败: " + e.getMessage());
        }
    }
}
