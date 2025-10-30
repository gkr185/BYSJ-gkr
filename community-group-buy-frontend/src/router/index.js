import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/profile'
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
