import request from './request'

/** 某项目下的申请列表 */
export const listApplications = (projectId) =>
  request.get('/applications', { params: { projectId } })

/** 单张申请详情 */
export const getApplication = (id) => request.get(`/applications/${id}`)

/**
 * 五个业务操作。
 *
 * 注意两点：
 *   1. 参数走 params（即 URL 后面的 ?a=1&b=2），后端用 @RequestParam 接收
 *   2. 中文（reason）由 axios 自动做 URL 编码，不用担心
 *
 * 也不传 operatorId —— 操作人由后端从会话里取。
 */
export const submitApplication = (id) =>
  request.post(`/applications/${id}/submit`)

export const approveApplication = (id, approvedAmount) =>
  request.post(`/applications/${id}/approve`, null, { params: { approvedAmount } })

export const rejectApplication = (id, reason) =>
  request.post(`/applications/${id}/reject`, null, { params: { reason } })

export const cancelApplication = (id, reason) =>
  request.post(`/applications/${id}/cancel`, null, { params: { reason } })

export const settleApplication = (id, actualAmount) =>
  request.post(`/applications/${id}/settle`, null, { params: { actualAmount } })
