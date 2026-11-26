import request from './request'

/** 登录：成功后后端会把用户写进 Session，并返回 Set-Cookie */
export const login = (username, password) =>
  request.post('/auth/login', { username, password })

/** 登出：销毁会话 */
export const logout = () => request.post('/auth/logout')

/** 当前登录用户；未登录时返回 null */
export const getCurrentUser = () => request.get('/auth/me')
