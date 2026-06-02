import { request } from './client'

/**
 * 跨五层知识检索（EXT-04）
 * @param {{ keyword: string, page?: number, pageSize?: number }} params
 */
export function searchKnowledge({ keyword, page = 1, pageSize = 10 }) {
  const qs = new URLSearchParams()
  qs.set('keyword', keyword)
  qs.set('page', String(page))
  qs.set('pageSize', String(pageSize))
  return request(`/knowledge/search?${qs}`)
}

/** 获取单条完整内容（用于展开） */
export async function fetchFullContent(layerKey, id) {
  const { getKnowledge } = await import('./knowledge.js')
  return getKnowledge(layerKey, id)
}
