import { request } from './client'

/** 获取五层元数据列表 */
export function listLayers() {
  return request('/layers')
}

/** 获取单层元数据 */
export function getLayer(layerKey) {
  return request(`/layers/${layerKey}`)
}

/** 更新单层元数据（P1） */
export function updateLayer(layerKey, body) {
  return request(`/layers/${layerKey}`, { method: 'PUT', body })
}
