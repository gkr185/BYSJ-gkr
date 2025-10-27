import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { title: '首页' }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/user/ProfileView.vue'),
      meta: { title: '个人中心' }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/user/info',
      name: 'userInfo',
      component: () => import('../views/user/UserInfoView.vue'),
      meta: { title: '个人信息', requireAuth: true }
    },
    {
      path: '/user/address',
      name: 'address',
      component: () => import('../views/user/AddressView.vue'),
      meta: { title: '收货地址', requireAuth: true }
    },
    {
      path: '/user/balance',
      name: 'balance',
      component: () => import('../views/user/BalanceView.vue'),
      meta: { title: '我的余额', requireAuth: true }
    },
    {
      path: '/user/feedback',
      name: 'feedback',
      component: () => import('../views/user/FeedbackView.vue'),
      meta: { title: '意见反馈', requireAuth: true }
    },
    // 商品相关路由
    {
      path: '/products',
      name: 'products',
      component: () => import('../views/product/ProductListView.vue'),
      meta: { title: '商品列表' }
    },
    {
      path: '/products/:id',
      name: 'product-detail',
      component: () => import('../views/product/ProductDetailView.vue'),
      meta: { title: '商品详情' }
    },
    // 拼团相关路由（v2.0优化版）
    {
      path: '/groupbuy/activity/:id',
      name: 'groupbuy-activity',
      component: () => import('../views/groupbuy/ActivityView.vue'),
      meta: { title: '拼团活动' }
    },
    {
      path: '/groupbuy/team/:id',
      name: 'groupbuy-team',
      component: () => import('../views/groupbuy/TeamView.vue'),
      meta: { title: '团详情' }
    },
    {
      path: '/groupbuy/my',
      name: 'my-groupbuy',
      component: () => import('../views/groupbuy/MyGroupBuyView.vue'),
      meta: { title: '我的拼团', requireAuth: true }
    },
    // 购物车
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/order/CartView.vue'),
      meta: { title: '购物车' }
    },
    // 订单相关路由
    {
      path: '/order/confirm',
      name: 'order-confirm',
      component: () => import('../views/order/OrderConfirmView.vue'),
      meta: { title: '确认订单', requireAuth: true }
    },
    {
      path: '/payment',
      name: 'payment',
      component: () => import('../views/payment/PaymentView.vue'),
      meta: { title: '支付订单', requireAuth: true }
    },
    {
      path: '/user/orders',
      name: 'orders',
      component: () => import('../views/order/OrderListView.vue'),
      meta: { 
        title: '我的订单',
        requireAuth: true
      }
    },
    {
      path: '/user/about',
      name: 'about',
      component: () => import('../views/user/PlaceholderView.vue'),
      meta: { 
        title: '关于我们',
        icon: 'info-o',
        description: '关于我们'
      }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title || '社区团购'
  
  // 需要登录的页面
  if (to.meta.requireAuth && !userStore.isLogin) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }
  
  next()
})

export default router
