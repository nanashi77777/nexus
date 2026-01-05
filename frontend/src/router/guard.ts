import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import router from './index'
import { useAuthStore } from '@/stores/auth'

// 白名单路径（不需要登录即可访问）
const whiteList = ['/login']

router.beforeEach(
  (to: RouteLocationNormalized, _from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const authStore = useAuthStore()
    
    // 检查是否已登录
    const isAuthenticated = authStore.isAuthenticated()
    
    // 如果在白名单中，直接放行
    if (whiteList.includes(to.path)) {
      next()
      return
    }
    
    // 如果未登录且访问的不是白名单页面，重定向到登录页
    if (!isAuthenticated) {
      next('/login')
      return
    }
    
    // 已登录，放行
    next()
  }
)
