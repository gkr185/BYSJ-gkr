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
    // 首页
    {
      path: '/home',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { title: '首页' }
    },
    // 商品相关
    {
      path: '/products',
      name: 'products',
      component: () => import('../views/product/ProductListView.vue'),
      meta: { title: '商品列表' }
    },
    {
      path: '/products/:id',
      name: 'productDetail',
      component: () => import('../views/product/ProductDetailView.vue'),
      meta: { title: '商品详情' }
    },
    // 购物车
    {
      path: '/cart',
      name: 'cart',
      component: () => import('../views/CartView.vue'),
      meta: { title: '购物车' }
    },
    // 拼团相关
    {
      path: '/groupbuy',
      name: 'groupbuy',
      component: () => import('../views/groupbuy/GroupBuyListView.vue'),
      meta: { title: '拼团活动' }
    },
    {
      path: '/groupbuy/team/:id',
      name: 'teamDetail',
      component: () => import('../views/groupbuy/TeamDetailView.vue'),
      meta: { title: '团详情' }
    },
    {
      path: '/user/groups',
      name: 'myGroups',
      component: () => import('../views/groupbuy/MyGroupBuysView.vue'),
      meta: { title: '我的拼团', requireAuth: true }
    },
    // 团长端相关
    {
      path: '/leader/apply',
      name: 'leaderApply',
      component: () => import('../views/leader/LeaderApplyView.vue'),
      meta: { title: '申请成为团长', requireAuth: true }
    },
    {
      path: '/community/apply',
      name: 'communityApply',
      component: () => import('../views/community/CommunityApplyView.vue'),
      meta: { title: '申请新社区', requireAuth: true }
    },
    // 团长功能页面（需要团长权限）
    {
      path: '/leader/dashboard',
      name: 'leaderDashboard',
      component: () => import('../views/leader/DashboardView.vue'),
      meta: { title: '团长工作台', requireAuth: true, requiresLeader: true }
    },
    {
      path: '/leader/activities',
      name: 'leaderActivities',
      component: () => import('../views/leader/ActivityManageView.vue'),
      meta: { title: '活动管理', requireAuth: true, requiresLeader: true }
    },
    {
      path: '/leader/activities/create',
      name: 'leaderCreateActivity',
      component: () => import('../views/leader/CreateActivityView.vue'),
      meta: { title: '创建活动', requireAuth: true, requiresLeader: true }
    },
    {
      path: '/leader/activities/edit/:id',
      name: 'leaderEditActivity',
      component: () => import('../views/leader/CreateActivityView.vue'),
      meta: { title: '编辑活动', requireAuth: true, requiresLeader: true }
    },
    {
      path: '/leader/launch',
      name: 'leaderLaunch',
      component: () => import('../views/leader/LaunchTeamView.vue'),
      meta: { title: '发起拼团', requireAuth: true, requiresLeader: true }
    },
    {
      path: '/leader/teams',
      name: 'leaderTeams',
      component: () => import('../views/leader/MyTeamsView.vue'),
      meta: { title: '我的团队', requireAuth: true, requiresLeader: true }
    },
    {
      path: '/leader/commission',
      name: 'leaderCommission',
      component: () => import('../views/leader/CommissionView.vue'),
      meta: { title: '佣金管理', requireAuth: true, requiresLeader: true }
    },
    {
      path: '/leader/store',
      name: 'leaderStore',
      component: () => import('../views/leader/StoreSettingsView.vue'),
      meta: { title: '团点设置', requireAuth: true, requiresLeader: true }
    },
    // 占位页面（需要后端支持）
    {
      path: '/leader/orders',
      name: 'leaderOrders',
      component: () => import('../views/leader/OrderManageView.vue'),
      meta: { title: '订单管理', requireAuth: true, requiresLeader: true }
    },
    {
      path: '/leader/delivery',
      name: 'leaderDelivery',
      component: () => import('../views/leader/DeliveryManageView.vue'),
      meta: { title: '配送管理', requireAuth: true, requiresLeader: true }
    },
    {
      path: '/leader/statistics',
      name: 'leaderStatistics',
      component: () => import('../views/leader/StatisticsView.vue'),
      meta: { title: '数据统计', requireAuth: true, requiresLeader: true }
    },
    // 订单相关
    {
      path: '/user/orders',
      name: 'myOrders',
      component: () => import('../views/order/MyOrdersView.vue'),
      meta: { title: '我的订单', requireAuth: true }
    },
    {
      path: '/user/orders/:id',
      name: 'orderDetail',
      component: () => import('../views/order/OrderDetailView.vue'),
      meta: { title: '订单详情', requireAuth: true }
    },
    // 登录
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { title: '登录' }
    },
    // 用户中心
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
