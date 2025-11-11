package com.bcu.edu.service;

import com.bcu.edu.client.ProductServiceClient;
import com.bcu.edu.client.GroupBuyServiceClient;
import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.AddCartRequest;
import com.bcu.edu.dto.request.CheckoutCartRequest;
import com.bcu.edu.dto.request.CreateOrderRequest;
import com.bcu.edu.dto.response.CartItemVO;
import com.bcu.edu.dto.response.ProductDTO;
import com.bcu.edu.entity.ShoppingCart;
import com.bcu.edu.repository.ShoppingCartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 购物车服务
 *
 * @author 耿康瑞
 * @since 2025-11-04
 */
@Service
@Slf4j
public class CartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ProductServiceClient productServiceClient;

    @Autowired
    private GroupBuyServiceClient groupBuyServiceClient;

    @Autowired
    private OrderService orderService;

    /**
     * 查询用户购物车列表
     */
    @OperationLog(value = "查询购物车", module = "购物车管理")
    public List<CartItemVO> getCartList(Long userId) {
        log.info("查询用户购物车: userId={}", userId);

        List<ShoppingCart> cartItems = shoppingCartRepository.findByUserId(userId);
        log.info("查询到{}个购物车项", cartItems.size());

        return cartItems.stream()
                .map(this::convertToCartItemVO)
                .collect(Collectors.toList());
    }

    /**
     * 添加商品到购物车
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "添加购物车", module = "购物车管理")
    public CartItemVO addToCart(Long userId, AddCartRequest request) {
        log.info("添加购物车: userId={}, productId={}, activityId={}, quantity={}",
                userId, request.getProductId(), request.getActivityId(), request.getQuantity());

        // 1. 验证商品存在性
        Result<ProductDTO> productResult = productServiceClient.getProduct(request.getProductId());
        if (productResult.getData() == null) {
            throw new BusinessException("商品不存在");
        }
        ProductDTO product = productResult.getData();

        // 2. 检查库存
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("商品库存不足");
        }

        // 3. 如果是拼团商品，验证活动存在性
        if (request.getActivityId() != null) {
            Result<Boolean> activityValid = groupBuyServiceClient.validateActivity(request.getActivityId());
            if (activityValid.getData() == null || !activityValid.getData()) {
                throw new BusinessException("拼团活动不存在");
            }
        }

        // 4. 检查是否已存在相同的购物车项（唯一索引保证不重复）
        Optional<ShoppingCart> existingCart = shoppingCartRepository
                .findByUserIdAndProductIdAndActivityId(userId, request.getProductId(), request.getActivityId());

        ShoppingCart cartItem;
        if (existingCart.isPresent()) {
            // 如果存在，更新数量
            cartItem = existingCart.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItem.setUpdateTime(LocalDateTime.now());
            log.info("更新购物车项数量: cartId={}, newQuantity={}", cartItem.getCartId(), cartItem.getQuantity());
        } else {
            // 如果不存在，创建新的购物车项
            cartItem = new ShoppingCart();
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setActivityId(request.getActivityId());
            cartItem.setQuantity(request.getQuantity());
            log.info("创建新的购物车项");
        }

        ShoppingCart savedCart = shoppingCartRepository.save(cartItem);
        log.info("购物车项保存成功: cartId={}", savedCart.getCartId());

        return convertToCartItemVO(savedCart);
    }

    /**
     * 更新购物车商品数量
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "更新购物车数量", module = "购物车管理")
    public CartItemVO updateCartQuantity(Long userId, Long cartId, Integer quantity) {
        log.info("更新购物车数量: userId={}, cartId={}, quantity={}", userId, cartId, quantity);

        // 1. 查找购物车项
        Optional<ShoppingCart> cartOptional = shoppingCartRepository.findById(cartId);
        if (cartOptional.isEmpty()) {
            throw new BusinessException("购物车项不存在");
        }

        ShoppingCart cartItem = cartOptional.get();

        // 2. 验证权限（只能修改自己的购物车）
        if (!cartItem.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此购物车项");
        }

        // 3. 验证数量
        if (quantity <= 0) {
            throw new BusinessException("数量必须大于0");
        }

        // 4. 检查库存
        Result<ProductDTO> productResult = productServiceClient.getProduct(cartItem.getProductId());
        if (productResult.getData() == null) {
            throw new BusinessException("商品不存在");
        }
        ProductDTO product = productResult.getData();

        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }

        // 5. 更新数量
        cartItem.setQuantity(quantity);
        cartItem.setUpdateTime(LocalDateTime.now());

        ShoppingCart savedCart = shoppingCartRepository.save(cartItem);
        log.info("购物车数量更新成功: cartId={}, quantity={}", cartId, quantity);

        return convertToCartItemVO(savedCart);
    }

    /**
     * 删除购物车商品
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "删除购物车商品", module = "购物车管理")
    public void removeFromCart(Long userId, Long cartId) {
        log.info("删除购物车商品: userId={}, cartId={}", userId, cartId);

        // 1. 查找购物车项
        Optional<ShoppingCart> cartOptional = shoppingCartRepository.findById(cartId);
        if (cartOptional.isEmpty()) {
            throw new BusinessException("购物车项不存在");
        }

        ShoppingCart cartItem = cartOptional.get();

        // 2. 验证权限（只能删除自己的购物车）
        if (!cartItem.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此购物车项");
        }

        // 3. 删除
        shoppingCartRepository.deleteById(cartId);
        log.info("购物车商品删除成功: cartId={}", cartId);
    }

    /**
     * 清空购物车
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "清空购物车", module = "购物车管理")
    public void clearCart(Long userId) {
        log.info("清空购物车: userId={}", userId);

        shoppingCartRepository.deleteByUserId(userId);
        log.info("购物车清空成功: userId={}", userId);
    }

    /**
     * 批量删除购物车商品
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "批量删除购物车", module = "购物车管理")
    public void batchRemoveCart(Long userId, List<Long> cartIds) {
        log.info("批量删除购物车: userId={}, cartIds={}", userId, cartIds);

        if (cartIds == null || cartIds.isEmpty()) {
            throw new BusinessException("购物车ID列表不能为空");
        }

        shoppingCartRepository.deleteByCartIdsAndUserId(cartIds, userId);
        log.info("批量删除购物车成功: userId={}, deletedCount={}", userId, cartIds.size());
    }

    /**
     * 统计购物车数量
     */
    public long getCartCount(Long userId) {
        log.info("统计购物车数量: userId={}", userId);

        long count = shoppingCartRepository.countByUserId(userId);
        log.info("购物车统计: userId={}, totalItems={}", userId, count);

        return count;
    }

    /**
     * 转换ShoppingCart为CartItemVO
     */
    private CartItemVO convertToCartItemVO(ShoppingCart cart) {
        CartItemVO vo = new CartItemVO();
        vo.setCartId(cart.getCartId());
        vo.setProductId(cart.getProductId());
        vo.setQuantity(cart.getQuantity());
        vo.setActivityId(cart.getActivityId());
        vo.setIsGroupBuy(cart.getActivityId() != null);
        vo.setAddTime(cart.getAddTime());

        // 获取商品信息
        Result<ProductDTO> productResult = productServiceClient.getProduct(cart.getProductId());
        if (productResult.getData() != null) {
            ProductDTO product = productResult.getData();
            vo.setProductName(product.getProductName());
            vo.setProductImg(product.getCoverImg());
            vo.setInStock(product.getStock() >= cart.getQuantity());

            // 设置价格：拼团商品使用拼团价，普通商品使用原价
            if (cart.getActivityId() != null) {
                // 获取拼团活动价格
                Result<BigDecimal> priceResult = groupBuyServiceClient.getActivityPrice(cart.getActivityId());
                if (priceResult.getData() != null) {
                    vo.setPrice(priceResult.getData());
                    vo.setGroupPrice(priceResult.getData()); // 拼团价格
                } else {
                    BigDecimal groupPrice = product.getGroupPrice() != null ? product.getGroupPrice() : product.getPrice();
                    vo.setPrice(groupPrice);
                    vo.setGroupPrice(groupPrice);
                }
            } else {
                vo.setPrice(product.getPrice());
                vo.setGroupPrice(null); // 普通商品没有拼团价格
            }

            // 计算小计
            vo.setSubtotal(vo.getPrice().multiply(new BigDecimal(cart.getQuantity())));
        } else {
            // 商品不存在的情况
            vo.setProductName("商品已下架");
            vo.setProductImg("");
            vo.setPrice(BigDecimal.ZERO);
            vo.setSubtotal(BigDecimal.ZERO);
            vo.setInStock(false);
        }

        return vo;
    }

    /**
     * 购物车结算（批量创建订单）
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "购物车结算", module = "购物车管理")
    public List<Long> checkoutCart(CheckoutCartRequest request) {
        return doCheckoutCart(request);
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public List<Long> doCheckoutCart(CheckoutCartRequest request) {
        log.info("开始购物车结算: cartIds={}, addressId={}, leaderId={}",
                request.getCartIds(), request.getAddressId(), request.getLeaderId());

        if (request.getCartIds() == null || request.getCartIds().isEmpty()) {
            throw new BusinessException("购物车ID列表不能为空");
        }

        List<Long> orderIds = new ArrayList<>();

        // 遍历每个购物车项，创建订单
        for (Long cartId : request.getCartIds()) {
            try {
                // 获取购物车项
                Optional<ShoppingCart> cartOptional = shoppingCartRepository.findById(cartId);
                if (cartOptional.isEmpty()) {
                    log.warn("购物车项不存在: cartId={}", cartId);
                    continue; // 跳过不存在的购物车项
                }

                ShoppingCart cart = cartOptional.get();

                // 获取商品信息以确定价格
                Result<ProductDTO> productResult = productServiceClient.getProduct(cart.getProductId());
                if (productResult.getData() == null) {
                    log.warn("商品不存在: productId={}", cart.getProductId());
                    continue;
                }

                ProductDTO product = productResult.getData();

                // 确定价格：拼团商品使用拼团价，普通商品使用原价
                BigDecimal price;
                if (cart.getActivityId() != null) {
                    // 获取拼团活动价格
                    Result<BigDecimal> priceResult = groupBuyServiceClient.getActivityPrice(cart.getActivityId());
                    if (priceResult.getData() != null) {
                        price = priceResult.getData();
                    } else {
                        price = product.getGroupPrice() != null ? product.getGroupPrice() : product.getPrice();
                    }
                } else {
                    price = product.getPrice();
                }

                // 创建订单请求
                CreateOrderRequest createOrderRequest = new CreateOrderRequest();
                createOrderRequest.setUserId(cart.getUserId()); // 从购物车项获取用户ID
                createOrderRequest.setProductId(cart.getProductId());
                createOrderRequest.setActivityId(cart.getActivityId());
                createOrderRequest.setQuantity(cart.getQuantity());
                createOrderRequest.setPrice(price);
                createOrderRequest.setAddressId(request.getAddressId());
                createOrderRequest.setLeaderId(request.getLeaderId());

                // 设置商品快照信息
                createOrderRequest.setProductName(product.getProductName());
                createOrderRequest.setProductImg(product.getCoverImg());

                // 创建订单
                Long orderId = orderService.createOrder(createOrderRequest);
                orderIds.add(orderId);

                // 删除已结算的购物车项
                shoppingCartRepository.deleteById(cartId);
                log.info("购物车项结算完成: cartId={}, orderId={}", cartId, orderId);

            } catch (Exception e) {
                log.error("结算购物车项失败: cartId={}, error={}", cartId, e.getMessage());
                // 继续处理其他购物车项，不因单个失败而中断整个结算流程
            }
        }

        log.info("购物车结算完成: 成功创建{}个订单", orderIds.size());
        return orderIds;
    }
}
