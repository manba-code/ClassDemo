import { request } from './client'

/** 获取知识图谱聚合数据（根 → 五层 → 知识点） */
export function getKnowledgeGraph() {
  return request('/knowledge-graph')
}
