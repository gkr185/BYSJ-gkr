import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // === 基础页面 ===
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
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { title: '注册' }
    },

    // === 商品详情 ===
    {
      path: '/products/:id',
      name: 'productDetail',
      component: () => import('../views/product/ProductDetailView.vue'),
      meta: { title: '商品详情' }
    },

    // === 拼团相关 ===
    {
      path: '/groupbuy/product/:productId',
      name: 'groupBuyProductDetail',
      component: () => import('../views/groupbuy/GroupBuyProductDetailView.vue'),
      meta: { title: '拼团详情' }
    },

    // === 用户中心 ===
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/user/ProfileView.vue'),
      meta: { title: '个人中心', requireAuth: true }
    },
    {
      path: '/user/info',
      name: 'userInfo',
      component: () => import('../views/user/UserInfoView.vue'),
      meta: { title: '个人资料', requireAuth: true }
    },
    {
      path: '/user/address',
      name: 'userAddress',
      component: () => import('../views/user/AddressView.vue'),
      meta: { title: '收货地址', requireAuth: true }
    },
    {
      path: '/user/balance',
      name: 'userBalance',
      component: () => import('../views/user/BalanceView.vue'),
      meta: { title: '账户余额', requireAuth: true }
    },
    {
      path: '/user/feedback',
      name: 'userFeedback',
      component: () => import('../views/user/FeedbackView.vue'),
      meta: { title: '意见反馈', requireAuth: true }
    },

    // === 团长相关 ===
    {
      path: '/leader/apply',
      name: 'leaderApply',
      component: () => import('../views/leader/LeaderApplyView.vue'),
      meta: { title: '申请成为团长', requireAuth: true }
    },
    {
      path: '/leader/dashboard',
      name: 'leaderDashboard',
      component: () => import('../views/leader/LeaderDashboardView.vue'),
      meta: { title: '团长工作台', requireAuth: true, requiresLeader: true }
    },

    // === 404 页面（必须放在最后） ===
    {
      path: '/:pathMatch(.*)*',
      name: 'notFound',
      component: () => import('../views/404NotFound.vue'),
      meta: { title: '页面未找到' }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title || '社区团购'
  
  // 检查团长权限（v3.0新增）
  if (to.meta.requiresLeader) {
    if (!userStore.isLogin) {
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    if (!userStore.isLeader) {
      ElMessage.warning('仅团长可访问此页面')
      next('/profile')
      return
    }
  }
  
  // 需要登录的页面
  if (to.meta.requireAuth && !userStore.isLogin) {
    ElMessage.warning('请先登录')
    next('/login')
    return
  }
  
  next()
})

export default router
