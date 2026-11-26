import request from './request'

/** 项目列表 */
export const listProjects = () => request.get('/projects')

/** 单个项目（含总预算、可用、冻结、已用） */
export const getProject = (id) => request.get(`/projects/${id}`)
