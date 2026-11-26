import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Workspace from '../views/Workspace.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: Login },
    { path: '/workspace', name: 'workspace', component: Workspace }
  ]
})

/**
 * 路由守卫：没登录就回登录页。
 *
 * 注意：这只是"体验优化"，不是安全措施。
 * 真正的权限在后端——就算有人手动改 localStorage 进到 workspace，
 * 任何业务接口都会因为没会话而返回 401。
 */
router.beforeEach((to) => {
  const logged = !!localStorage.getItem('loginUser')
  if (to.path !== '/login' && !logged) {
    return '/login'
  }
  if (to.path === '/login' && logged) {
    return '/workspace'
  }
  return true
})

export default router
