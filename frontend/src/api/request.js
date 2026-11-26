import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 统一的请求封装。
 *
 * 做了三件事：
 *   1. baseURL 设为 /api —— 后面的请求只写 '/members' 即可
 *      （配合 vite.config.js 的代理，实际会转发到 http://localhost:8080/api/members）
 *   2. 拦截响应，把 {code,message,data} 拆开：
 *      业务成功直接返回 data，业务失败弹提示并 reject
 *   3. 遇到 401 自动清掉本地登录态并跳回登录页
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true // 会话靠 Cookie，必须带上
})

request.interceptors.response.use(
  (resp) => {
    const body = resp.data
    // 后端成功时 code = 0
    if (body && body.code === 0) {
      return body.data
    }
    ElMessage.error(body?.message || '请求失败')
    return Promise.reject(body)
  },
  (error) => {
    const status = error.response?.status
    const body = error.response?.data

    if (status === 401) {
      ElMessage.error('登录已失效，请重新登录')
      localStorage.removeItem('loginUser')
      if (location.pathname !== '/login') {
        location.href = '/login'
      }
    } else if (status === 403) {
      ElMessage.error(body?.message || '没有权限执行该操作')
    } else {
      ElMessage.error(body?.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default request
