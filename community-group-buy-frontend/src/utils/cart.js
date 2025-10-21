// 购物车工具函数 - 使用LocalStorage存储

const CART_KEY = 'group_buy_cart'

// 获取购物车数据
export function getCart() {
  const cartData = localStorage.getItem(CART_KEY)
  return cartData ? JSON.parse(cartData) : []
}

// 保存购物车数据
export function saveCart(cart) {
  localStorage.setItem(CART_KEY, JSON.stringify(cart))
  // 触发自定义事件，通知其他组件购物车已更新
  window.dispatchEvent(new CustomEvent('cart-updated', { detail: cart }))
}

// 添加商品到购物车
export function addToCart(product, quantity = 1) {
  const cart = getCart()
  const existingItem = cart.find(item => item.product_id === product.product_id)
  
  if (existingItem) {
    existingItem.quantity += quantity
    existingItem.update_time = new Date().toISOString()
  } else {
    cart.push({
      cart_id: Date.now(), // 临时ID
      product_id: product.product_id,
      product_name: product.product_name,
      cover_img: product.cover_img,
      price: product.price,
      group_price: product.group_price,
      quantity: quantity,
      stock: product.stock,
      add_time: new Date().toISOString(),
      update_time: new Date().toISOString()
    })
  }
  
  saveCart(cart)
  return cart
}

// 更新购物车商品数量
export function updateCartItemQuantity(productId, quantity) {
  const cart = getCart()
  const item = cart.find(item => item.product_id === productId)
  
  if (item) {
    item.quantity = quantity
    item.update_time = new Date().toISOString()
    saveCart(cart)
  }
  
  return cart
}

// 删除购物车商品
export function removeCartItem(productId) {
  let cart = getCart()
  cart = cart.filter(item => item.product_id !== productId)
  saveCart(cart)
  return cart
}

// 清空购物车
export function clearCart() {
  saveCart([])
  return []
}

// 计算购物车总价
export function calculateTotal(cart) {
  return cart.reduce((total, item) => {
    const price = item.group_price || item.price
    return total + (price * item.quantity)
  }, 0)
}

// 获取购物车商品数量
export function getCartCount() {
  const cart = getCart()
  return cart.reduce((count, item) => count + item.quantity, 0)
}

