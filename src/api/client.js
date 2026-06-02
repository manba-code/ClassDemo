const API_BASE = '/api'

/**
 * 统一请求封装，解析 ApiResponse { code, message, data }
 * @param {string} path 相对 /api 的路径，如 /layers
 * @param {{ method?: string, body?: object, headers?: Record<string, string> }} [options]
 * @returns {Promise<unknown>} 成功时返回 data 字段
 */
export async function request(path, options = {}) {
  const { method = 'GET', body, headers = {} } = options
  const init = {
    method,
    headers: {
      ...headers,
    },
  }

  if (body !== undefined && body !== null) {
    init.headers['Content-Type'] = 'application/json'
    init.body = JSON.stringify(body)
  }

  const res = await fetch(`${API_BASE}${path}`, init)
  let json
  try {
    json = await res.json()
  } catch {
    throw new Error(`请求失败：HTTP ${res.status}`)
  }

  if (json.code !== 0) {
    const err = new Error(json.message || '请求失败')
    err.code = json.code
    throw err
  }

  return json.data
}
