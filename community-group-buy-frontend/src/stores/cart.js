import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  // 购物车商品列表
  const items = ref([])
  
  // 从localStorage加载购物车
  const loadCart = () => {
    try {
      const savedCart = localStorage.getItem('shopping_cart')
      if (savedCart) {
        items.value = JSON.parse(savedCart)
      }
    } catch (error) {
      console.error('Failed to load cart:', error)
      items.value = []
    }
  }
  
  // 保存购物车到localStorage
  const saveCart = () => {
    try {
      localStorage.setItem('shopping_cart', JSON.stringify(items.value))
    } catch (error) {
      console.error('Failed to save cart:', error)
    }
  }
  
  // 计算总数量
  const totalCount = computed(() => {
    return items.value.reduce((total, item) => total + item.quantity, 0)
  })
  
  // 计算总价格
  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => {
      const price = item.groupPrice || item.price
      return total + price * item.quantity
    }, 0)
  })
  
  // 计算选中商品的总价
  const selectedTotalPrice = computed(() => {
    return items.value
      .filter(item => item.checked)
      .reduce((total, item) => {
        const price = item.groupPrice || item.price
        return total + price * item.quantity
      }, 0)
  })
  
  // 计算选中商品的数量
  const selectedCount = computed(() => {
    return items.value.filter(item => item.checked).length
  })
  
  // 全选状态
  const isAllSelected = computed(() => {
    return items.value.length > 0 && items.value.every(item => item.checked)
  })
  
  // 添加商品到购物车
  const addItem = (product, quantity = 1) => {
    const existingItem = items.value.find(item => item.productId === product.productId)
    
    if (existingItem) {
      // 已存在，增加数量
      existingItem.quantity += quantity
    } else {
      // 新商品
      items.value.push({
        productId: product.productId,
        productName: product.productName,
        coverImg: product.coverImg,
        price: product.price,
        groupPrice: product.groupPrice,
        stock: product.stock,
        quantity: quantity,
        checked: true
      })
    }
    
    saveCart()
  }
  
  // 更新商品数量
  const updateQuantity = (productId, quantity) => {
    const item = items.value.find(item => item.productId === productId)
    if (item) {
      if (quantity <= 0) {
        removeItem(productId)
      } else {
        item.quantity = Math.min(quantity, item.stock)
        saveCart()
      }
    }
  }
  
  // 移除商品
  const removeItem = (productId) => {
    const index = items.value.findIndex(item => item.productId === productId)
    if (index > -1) {
      items.value.splice(index, 1)
      saveCart()
    }
  }
  
  // 切换商品选中状态
  const toggleItemCheck = (productId) => {
    const item = items.value.find(item => item.productId === productId)
    if (item) {
      item.checked = !item.checked
      saveCart()
    }
  }
  
  // 全选/取消全选
  const toggleAllCheck = () => {
    const allChecked = isAllSelected.value
    items.value.forEach(item => {
      item.checked = !allChecked
    })
    saveCart()
  }
  
  // 清空购物车
  const clearCart = () => {
    items.value = []
    saveCart()
  }
  
  // 移除选中的商品
  const removeSelectedItems = () => {
    items.value = items.value.filter(item => !item.checked)
    saveCart()
  }
  
  // 获取选中的商品
  const getSelectedItems = () => {
    return items.value.filter(item => item.checked)
  }
  
  // 初始化时加载购物车
  loadCart()
  
  return {
    items,
    totalCount,
    totalPrice,
    selectedTotalPrice,
    selectedCount,
    isAllSelected,
    addItem,
    updateQuantity,
    removeItem,
    toggleItemCheck,
    toggleAllCheck,
    clearCart,
    removeSelectedItems,
    getSelectedItems,
    loadCart
  }
})

