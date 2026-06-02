import { request } from './client'

/**
 * 分页查询知识点列表
 * @param {string} layerKey
 * @param {{ page?: number, pageSize?: number, keyword?: string }} [params]
 */
export function listKnowledge(layerKey, { page = 1, pageSize = 10, keyword = '' } = {}) {
  const qs = new URLSearchParams()
  qs.set('page', String(page))
  qs.set('pageSize', String(pageSize))
  if (keyword) qs.set('keyword', keyword)
  return request(`/layers/${layerKey}/knowledge?${qs}`)
}

/** 获取知识点详情 */
export function getKnowledge(layerKey, id) {
  return request(`/layers/${layerKey}/knowledge/${id}`)
}

/** 新增知识点 */
export function createKnowledge(layerKey, body) {
  return request(`/layers/${layerKey}/knowledge`, { method: 'POST', body })
}

/** 更新知识点 */
export function updateKnowledge(layerKey, id, body) {
  return request(`/layers/${layerKey}/knowledge/${id}`, { method: 'PUT', body })
}

/** 删除知识点 */
export function deleteKnowledge(layerKey, id) {
  return request(`/layers/${layerKey}/knowledge/${id}`, { method: 'DELETE' })
}
