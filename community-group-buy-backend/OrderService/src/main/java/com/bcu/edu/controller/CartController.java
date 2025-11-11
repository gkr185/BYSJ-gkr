package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.AddCartRequest;
import com.bcu.edu.dto.request.CheckoutCartRequest;
import com.bcu.edu.dto.response.CartItemVO;
import com.bcu.edu.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车Controller
 * 对应前端 cart.js API接口
 *
 * @author 耿康瑞
 * @since 2025-11-04
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "购物车管理", description = "购物车添加、删除、修改、查询等")
@Slf4j
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 查询购物车列表
     * 对应前端: getCartList(userId)
     */
    @GetMapping("/{userId}")
    @Operation(summary = "查询购物车列表", description = "查询用户的购物车商品列表")
    @OperationLog(value = "查询购物车", module = "购物车管理")
    public Result<List<CartItemVO>> getCartList(@PathVariable Long userId) {
        log.info("查询购物车列表: userId={}", userId);

        List<CartItemVO> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }

    /**
     * 添加商品到购物车
     * 对应前端: addToCart(data)
     */
    @PostMapping("/{userId}")
    @Operation(summary = "添加商品到购物车", description = "添加商品到用户的购物车")
    @OperationLog(value = "添加购物车", module = "购物车管理")
    public Result<CartItemVO> addToCart(@PathVariable Long userId, @RequestBody AddCartRequest request) {
        log.info("添加商品到购物车: userId={}, productId={}, activityId={}, quantity={}",
                userId, request.getProductId(), request.getActivityId(), request.getQuantity());

        CartItemVO cartItem = cartService.addToCart(userId, request);
        return Result.success(cartItem);
    }

    /**
     * 更新购物车商品数量
     * 对应前端: updateCartQuantity(userId, cartId, quantity)
     */
    @PutMapping("/{userId}/{cartId}")
    @Operation(summary = "更新购物车商品数量", description = "更新购物车中商品的数量")
    @OperationLog(value = "更新购物车数量", module = "购物车管理")
    public Result<CartItemVO> updateCartQuantity(@PathVariable Long userId,
                                               @PathVariable Long cartId,
                                               @RequestParam Integer quantity) {
        log.info("更新购物车商品数量: userId={}, cartId={}, quantity={}", userId, cartId, quantity);

        CartItemVO cartItem = cartService.updateCartQuantity(userId, cartId, quantity);
        return Result.success(cartItem);
    }

    /**
     * 删除购物车商品
     * 对应前端: removeFromCart(userId, cartId)
     */
    @DeleteMapping("/{userId}/{cartId}")
    @Operation(summary = "删除购物车商品", description = "从购物车中删除指定的商品")
    @OperationLog(value = "删除购物车商品", module = "购物车管理")
    public Result<Void> removeFromCart(@PathVariable Long userId, @PathVariable Long cartId) {
        log.info("删除购物车商品: userId={}, cartId={}", userId, cartId);

        cartService.removeFromCart(userId, cartId);
        return Result.success("商品已从购物车删除");
    }

    /**
     * 清空购物车
     * 对应前端: clearCart(userId)
     */
    @DeleteMapping("/{userId}/clear")
    @Operation(summary = "清空购物车", description = "清空用户的所有购物车商品")
    @OperationLog(value = "清空购物车", module = "购物车管理")
    public Result<Void> clearCart(@PathVariable Long userId) {
        log.info("清空购物车: userId={}", userId);

        cartService.clearCart(userId);
        return Result.success("购物车已清空");
    }

    /**
     * 批量删除购物车商品
     * 对应前端: batchRemoveCart(userId, cartIds)
     */
    @DeleteMapping("/{userId}/batch")
    @Operation(summary = "批量删除购物车商品", description = "批量删除购物车中的多个商品")
    @OperationLog(value = "批量删除购物车", module = "购物车管理")
    public Result<Void> batchRemoveCart(@PathVariable Long userId, @RequestBody List<Long> cartIds) {
        log.info("批量删除购物车商品: userId={}, cartIds={}", userId, cartIds);

        cartService.batchRemoveCart(userId, cartIds);
        return Result.success("批量删除成功");
    }

    /**
     * 统计购物车数量
     * 对应前端: getCartCount(userId)
     */
    @GetMapping("/{userId}/count")
    @Operation(summary = "统计购物车数量", description = "统计用户购物车中的商品总数量")
    public Result<Long> getCartCount(@PathVariable Long userId) {
        log.info("统计购物车数量: userId={}", userId);

        long count = cartService.getCartCount(userId);
        return Result.success(count);
    }

    /**
     * 购物车结算（批量创建订单）
     * 对应前端: checkoutCart(data)
     */
    @PostMapping("/checkout")
    @Operation(summary = "购物车结算", description = "批量结算购物车商品，创建订单")
    @OperationLog(value = "购物车结算", module = "购物车管理")
    public Result<List<Long>> checkoutCart(@RequestBody CheckoutCartRequest request) {
        log.info("购物车结算: cartIds={}, addressId={}, leaderId={}",
                request.getCartIds(), request.getAddressId(), request.getLeaderId());

        List<Long> orderIds = cartService.checkoutCart(request);
        log.info("购物车结算完成，创建订单数量: {}", orderIds.size());

        return Result.success(orderIds);
    }
}
