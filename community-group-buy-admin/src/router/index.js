import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import LayoutView from '../views/LayoutView.vue'
import UserManageView from '../views/UserManageView.vue'
import FeedbackManageView from '../views/FeedbackManageView.vue'

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
