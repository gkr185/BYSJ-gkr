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
}
