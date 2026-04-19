import { createRouter, createWebHistory } from 'vue-router'
import { jwtDecode } from 'jwt-decode'
import TestView from '../views/TestView.vue'
import Register from '../views/Register.vue'
import Login from '../views/Login.vue'
import TheoremList from '../views/TheoremList.vue'
import TheoremDetail from '@/views/TheoremDetail.vue'
import Profile from '../views/Profile.vue'
import QuestionBank from '../views/QuestionBank.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', component: Register },                // 根路径固定显示注册页
    { path: '/register', name: 'Register', component: Register },
    { path: '/login', name: 'Login', component: Login },
    { path: '/theorems', name: 'TheoremList', component: TheoremList },
    { path: '/theorem/:id', component: TheoremDetail, name: 'theorem-detail' },
    { path: '/profile', name: 'Profile', component: Profile },
    { path: '/test/:testId', name: 'TestView', component: TestView },
    { path: '/questions', name: 'QuestionBank', component: QuestionBank },
    { path: '/practice', name: 'Practice', component: TestView }
  ]
})

// 增强路由守卫：验证 token 是否存在且未过期
router.beforeEach((to, from, next) => {
  let token = localStorage.getItem('token')
  let isValid = false

  if (token) {
    try {
      // 解码 token，获取过期时间（单位：秒）
      const decoded = jwtDecode<{ exp: number }>(token)
      // 检查是否过期（当前时间 > 过期时间）
      isValid = decoded.exp > Date.now() / 1000
    } catch (error) {
      console.error('Token 解析失败', error)
      isValid = false
    }
  }

  // 如果 token 无效（不存在或过期），清除本地存储的 token
  if (!isValid) {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    token = null
  }

  // 公开页面列表（无需登录即可访问）
  const publicPages = ['/', '/login', '/register']
  const isPublicPage = publicPages.includes(to.path)

  // 需要登录但 token 无效时，重定向到登录页
  if (!isPublicPage && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router