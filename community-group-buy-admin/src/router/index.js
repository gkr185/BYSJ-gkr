import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import LayoutView from '../views/LayoutView.vue'
import UserManageView from '../views/UserManageView.vue'
import FeedbackManageView from '../views/FeedbackManageView.vue'
import LogManageView from '../views/LogManageView.vue'
import CommunityManageView from '../views/CommunityManageView.vue'
import CommunityApplicationView from '../views/CommunityApplicationView.vue'
import LeaderManageView from '../views/LeaderManageView.vue'
import CommissionManageView from '../views/CommissionManageView.vue'
import ProductManageView from '../views/ProductManageView.vue'
import CategoryManageView from '../views/CategoryManageView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: LayoutView,
      meta: { requiresAuth: true },
      redirect: '/users',
      children: [
        {
          path: '/users',
          name: 'users',
          component: UserManageView
        },
        {
          path: '/feedback',
          name: 'feedback',
          component: FeedbackManageView
        },
        {
          path: '/logs',
          name: 'logs',
          component: LogManageView,
          meta: { title: '系统日志', role: 'ADMIN' }
        },
        {
          path: '/community',
          name: 'community',
          component: CommunityManageView,
          meta: { title: '社区管理' }
        },
        {
          path: '/community-application',
          name: 'communityApplication',
          component: CommunityApplicationView,
          meta: { title: '社区申请审核' }
        },
        {
          path: '/leader',
          name: 'leader',
          component: LeaderManageView,
          meta: { title: '团长管理' }
        },
        {
          path: '/commission',
          name: 'commission',
          component: CommissionManageView,
          meta: { title: '佣金管理' }
        },
        {
          path: '/products',
          name: 'products',
          component: ProductManageView,
          meta: { title: '商品管理' }
        },
        {
          path: '/categories',
          name: 'categories',
          component: CategoryManageView,
          meta: { title: '分类管理' }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录，跳转到登录页
    next('/login')
  } else if (to.path === '/login' && token) {
    // 已登录访问登录页，跳转到首页
    next('/users')
  } else {
    next()
  }
})

export default router
