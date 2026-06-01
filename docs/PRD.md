# 计算机网络知识体系交互式展示系统 — 产品需求文档（PRD）

| 文档版本 | v1.0 |
|---------|------|
| 编写日期 | 2026-06-01 |
| 适用课程 | 《网络实习》 |
| 项目名称 | 计算机网络知识体系交互式展示系统 |
| 文档状态 | 初稿 |

---

## 1. 文档说明

### 1.1 目的

本文档依据《网络实习》任务与指导书，将实习作业要求转化为可执行的产品需求、页面规格、数据模型与前后端接口约定，供开发、测试与验收使用。

### 1.2 范围

| 模块 | 是否需要后端 | 说明 |
|------|-------------|------|
| 第一部分：单项协议可视化 | 否 | 纯前端交互与动画 |
| 第二部分：综合网络场景模拟 | 否 | 纯前端分步演示 |
| 第三部分：知识体系与知识图谱 | 是 | 知识点 CRUD + 数据库持久化 |
| 系统导航与首页 | 否 | 路由与模块入口 |

### 1.3 已确认选型（本项目）

| 项 | 选型 |
|----|------|
| 第一部分协议 | **题目 2：TCP 三次握手与四次挥手**（已实现初版）+ **题目 1：DNS 域名解析**（待实现，可替换为其他题目） |
| 前端 | Vue 3 + Vite + Vue Router + Tailwind CSS |
| 后端 | Node.js + Express |
| 数据库 | SQLite（开发/演示）+ 建表 SQL 脚本 |
| 知识图谱 | 前端 JS 渲染（不要求图数据库存储） |

---

## 2. 产品概述

### 2.1 产品定位

面向计算机网络课程学习的 **Web 交互式教学系统**，集协议过程可视化、综合通信场景模拟、TCP/IP 五层知识体系管理与知识图谱展示于一体。

### 2.2 目标用户

- **主要用户**：计算机科学与技术专业学生（课程实习学习者）
- **次要用户**：指导教师（验收、演示、评分）

### 2.3 产品目标

1. 将抽象网络协议转化为可观察、可操作的动态演示。
2. 完整模拟 H1 访问 `www.abc.com` 从 t0 到 t1 的通信过程。
3. 以数据库驱动的方式管理五层模型知识点，支持增删改查与模糊查询。
4. 通过知识图谱呈现整体知识体系结构。

### 2.4 成功标准（对齐评分要点）

- 至少 2 个协议可视化页面可正常运行。
- 综合场景步骤拆解正确，ARP 表与交换表动态更新。
- 五层模型 6 个子标签页可用，知识点修改后刷新不丢失。
- 具备首页导航，模块间可跳转。
- README、建表脚本、演示截图齐全。

---

## 3. 信息架构与页面地图

```
/                           首页（系统介绍 + 模块导航）
├── /protocol               协议可视化（子导航）
│   ├── /protocol/tcp       题目2：TCP 三次握手 / 四次挥手
│   └── /protocol/dns       题目1：DNS 域名解析
├── /scenario               第二部分：综合网络场景模拟
└── /knowledge              第三部分：知识体系
    ├── /knowledge/application   应用层
    ├── /knowledge/transport     传输层
    ├── /knowledge/network       网络层
    ├── /knowledge/datalink      数据链路层
    ├── /knowledge/physical      物理层
    └── /knowledge/graph         知识图谱
```

---

## 4. 功能需求

### 4.1 公共模块

#### 4.1.1 首页 / 导航

| 编号 | 需求 | 优先级 |
|------|------|--------|
| COM-01 | 展示系统名称、简介、三大模块入口卡片 | P0 |
| COM-02 | 顶部或侧边全局导航，支持各主模块跳转 | P0 |
| COM-03 | 当前路由高亮，支持浏览器前进/后退 | P1 |
| COM-04 | 响应式布局，1280px 及以上为桌面优先 | P2 |

#### 4.1.2 通用交互规范（协议类页面）

所有协议可视化页面须满足：

| 编号 | 需求 | 优先级 |
|------|------|--------|
| COM-10 | 简洁网络拓扑图 | P0 |
| COM-11 | 「开始 / 下一步 / 重置」控制按钮 | P0 |
| COM-12 | 协议执行步骤列表或时间线 | P0 |
| COM-13 | 报文/数据帧在拓扑中的传递动画 | P0 |
| COM-14 | 关键状态表或结果面板 | P0 |
| COM-15 | 禁止仅静态文字/图片，必须有动态交互 | P0 |

---

### 4.2 第一部分：单项协议原理可视化（20 分）

#### 4.2.1 题目 2 — TCP 三次握手与四次挥手

**路由**：`/protocol/tcp`

| 编号 | 需求 | 优先级 | 验收标准 |
|------|------|--------|----------|
| TCP-01 | 展示 Client、Server 两端节点 | P0 | 拓扑清晰标注角色 |
| TCP-02 | 提供「建立连接」「释放连接」按钮 | P0 | 文案与指导书一致 |
| TCP-03 | 提供「开始/下一步/重置」分步控制 | P0 | 可单步推进 |
| TCP-04 | 动画展示 SYN、SYN+ACK、ACK、FIN 报文传递 | P0 | 方向 C→S / S→C 可辨 |
| TCP-05 | 实时展示 TCP 状态：CLOSED、LISTEN、SYN-SENT、SYN-RCVD、ESTABLISHED、FIN-WAIT-1/2、CLOSE-WAIT、LAST-ACK、TIME-WAIT | P0 | 每步状态与 RFC 793 一致 |
| TCP-06 | 展示 seq/ack 及 ISN | P1 | 报文详情面板 |
| TCP-07 | 握手/挥手完成后展示最终结果摘要 | P0 | 明确「连接已建立/已关闭」 |
| TCP-08 | 报文详情含源/目的 MAC、IP（可选字段） | P2 | 增强展示 |

**状态机参考（建立连接）**

```
Client: CLOSED → SYN-SENT → ESTABLISHED
Server: LISTEN → SYN-RCVD → ESTABLISHED
```

**状态机参考（释放连接）**

```
Client: ESTABLISHED → FIN-WAIT-1 → FIN-WAIT-2 → TIME-WAIT → CLOSED
Server: ESTABLISHED → CLOSE-WAIT → LAST-ACK → CLOSED
```

> **现状**：`TcpHandshakeVisualizer.vue` 已实现核心逻辑，需将按钮文案调整为「建立连接」「释放连接」，并接入路由导航。

---

#### 4.2.2 题目 1 — DNS 域名解析（第二协议，待实现）

**路由**：`/protocol/dns`

| 编号 | 需求 | 优先级 | 验收标准 |
|------|------|--------|----------|
| DNS-01 | 展示 Client、DNS Server，域名输入框 | P0 | 用户可输入域名 |
| DNS-02 | 点击查询后分步展示 DNS 查询过程 | P0 | 支持开始/下一步/重置 |
| DNS-03 | 支持演示「缓存命中」与「缓存未命中」两种模式 | P0 | 可通过切换或首次/二次查询体现 |
| DNS-04 | 展示最终解析 IP 地址 | P0 | 结果面板明确显示 |
| DNS-05 | DNS 缓存表动态查询与更新 | P0 | 表格随步骤变化 |
| DNS-06 | 报文在 Client 与 DNS Server 间传递动画 | P1 | 与 TCP 页风格统一 |

**参考拓扑**

```
[Client] ---- [Local DNS Server]
                |
           (递归/迭代查询，可选展示)
```

---

### 4.3 第二部分：综合网络场景模拟（20 分）

**路由**：`/scenario`

#### 4.3.1 场景定义

**拓扑要素**

| 设备 | 说明 |
|------|------|
| H1 | 发起 Web 访问的主机 |
| H2 | 同网段其他主机（背景节点） |
| S | 以太网交换机 |
| 本地 DNS | 连接在 S 上 |
| R | 路由器，连接 S 与 Internet |
| Web Server | 位于 Internet，`www.abc.com` |

**初始条件**

- H1 的 ARP 表：空
- 交换机 S 的 MAC 地址表：空
- t0：H1 浏览器访问 `www.abc.com`
- t1：交换机 S **第一次**收到封装 HTTP 请求报文的以太网帧
- t0～t1 期间无无关通信

**建议地址规划（可在实现中固定，PRD 作为参考）**

| 设备 | IP | MAC |
|------|-----|-----|
| H1 | 192.168.1.10/24 | AA:AA:AA:AA:AA:01 |
| H2 | 192.168.1.20/24 | AA:AA:AA:AA:AA:02 |
| 本地 DNS | 192.168.1.53/24 | AA:AA:AA:AA:AA:53 |
| 路由器 R（内网口） | 192.168.1.1/24 | AA:AA:AA:AA:AA:FE |
| Web Server | 203.0.113.80 | （经 NAT 对外，内网不可见 MAC） |
| www.abc.com 解析结果 | 203.0.113.80 | — |

#### 4.3.2 功能需求

| 编号 | 需求 | 优先级 |
|------|------|--------|
| SCN-01 | 拓扑图展示 H1、H2、S、本地 DNS、R、Internet、Web Server | P0 |
| SCN-02 | 支持「开始 / 下一步 / 上一步 / 重置」 | P0 |
| SCN-03 | 每步展示：步骤编号、名称、起点、终点、协议、通信目的 | P0 |
| SCN-04 | 每步展示：广播/单播、源/目的 MAC、源/目的 IP | P0 |
| SCN-05 | 拓扑路径高亮当前数据流向 | P0 |
| SCN-06 | 展示交换机 S 的处理行为（学习、泛洪、转发） | P0 |
| SCN-07 | 每步结束后更新 H1 ARP 表 | P0 |
| SCN-08 | 每步结束后更新交换机 S 交换表 | P0 |
| SCN-09 | 全部步骤完成后展示 t0→t1 最终状态与结论 | P0 |
| SCN-10 | 步骤数据以前端 JSON 配置驱动（无需后端） | P1 |

#### 4.3.3 参考步骤划分（实现可微调，须逻辑正确）

> 学生可自行划分步骤，以下为推荐参考序列，共约 12～18 步。

| 步骤 | 名称 | 协议 | 概要 |
|------|------|------|------|
| 1 | H1 解析域名需求 | — | 浏览器需访问 www.abc.com |
| 2 | H1 ARP 查询网关 | ARP | 广播查询 R 的 MAC |
| 3 | R 回复 ARP | ARP | 单播 Reply，H1 写入 ARP 表 |
| 4 | H1 向本地 DNS 发 DNS 查询 | DNS/UDP | 经 S 转发 |
| 5 | 本地 DNS 返回 A 记录 | DNS/UDP | 得到 203.0.113.80 |
| 6 | H1 向 Web Server 发 SYN | TCP | 经 R 路由至 Internet |
| 7 | Web Server 回复 SYN+ACK | TCP | |
| 8 | H1 发 ACK | TCP | 三次握手完成 |
| 9 | H1 发送 HTTP GET 请求 | HTTP/TCP | |
| 10 | HTTP 请求封装为以太网帧到达 S（t1） | Ethernet | **t1 时刻** |

**t1 结论示例**：交换机 S 首次收到目的为 Web Server 方向、载荷为 HTTP 请求的以太网帧；此时 H1 ARP 表含网关 MAC，交换表已学习 H1、DNS 等端口映射。

---

### 4.4 第三部分：知识体系与知识图谱（15 分）

**路由**：`/knowledge/*`

#### 4.4.1 五层模型子页面（应用/传输/网络/数据链路/物理）

每个分层 Tab 页须展示：

| 编号 | 需求 | 优先级 | 数据来源 |
|------|------|--------|----------|
| KN-01 | 本层主要功能 | P0 | 后端 `layer_info` 或前端静态 + API |
| KN-02 | 本层常见协议或技术 | P0 | 同上 |
| KN-03 | 本层相关设备或数据单位 | P0 | 同上 |
| KN-04 | 知识点列表（分页） | P0 | 后端 API |
| KN-05 | 新增知识点 | P0 | POST API |
| KN-06 | 编辑知识点 | P0 | PUT API |
| KN-07 | 删除知识点 | P0 | DELETE API |
| KN-08 | 关键词模糊查询 | P0 | GET API `keyword` 参数 |
| KN-09 | 修改持久化，刷新不丢失 | P0 | SQLite |

**知识点字段**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | INTEGER | 自动 | 主键 |
| title | TEXT | 是 | 知识点标题 |
| content | TEXT | 是 | 详细内容 |
| tags | TEXT | 否 | 标签，逗号分隔 |
| sort_order | INTEGER | 否 | 排序权重，默认 0 |
| created_at | DATETIME | 自动 | 创建时间 |
| updated_at | DATETIME | 自动 | 更新时间 |

#### 4.4.2 知识图谱页

**路由**：`/knowledge/graph`

| 编号 | 需求 | 优先级 |
|------|------|--------|
| KG-01 | 树形/力导向图展示：计算机网络 → 五层 → 协议/设备/知识点 | P0 |
| KG-02 | 点击节点展示知识点详情（标题 + 内容） | P0 |
| KG-03 | 数据来源于各层知识点 API 聚合，前端组装 | P1 |
| KG-04 | 不要求图结构持久化到数据库 | — |

---

### 4.5 附加功能（加分项，最高 5 分）

可选实现，PRD 预留扩展：

| 编号 | 功能 | 说明 |
|------|------|------|
| EXT-01 | 网络故障模拟 | DNS 失败、ARP 错误、TCP 超时 |
| EXT-02 | 协议过程回放 | 暂停、回放、单步 |
| EXT-03 | 报文结构解析 | Ethernet / IP / TCP / UDP / HTTP 字段 |
| EXT-04 | 知识检索问答 | 基于知识点 keyword 搜索增强 |

---

## 5. 非功能需求

| 编号 | 类别 | 要求 |
|------|------|------|
| NFR-01 | 兼容性 | Chrome / Edge 最新版正常 |
| NFR-02 | 性能 | 协议动画流畅，单页切换 < 500ms |
| NFR-03 | 可维护性 | 前后端分离，目录结构清晰 |
| NFR-04 | 安全 | 本地教学演示，无鉴权；生产环境不在范围 |
| NFR-05 | 部署 | `npm run dev` 启动前端；后端独立端口；提供 README |
| NFR-06 | 规范 | Git 分阶段 commit，禁止一次性提交全部代码 |

---

## 6. 数据库设计

### 6.1 ER 关系概览

```
layer_info (1) ──────< knowledge_* (每层独立表)
     │
     └── 五层各一条元数据记录

knowledge_* 五张表结构相同，分表存储（符合指导书要求）
```

### 6.2 表：`layer_info`（分层元数据）

```sql
CREATE TABLE layer_info (
  id          INTEGER PRIMARY KEY,  -- 1~5 对应五层
  layer_key   TEXT NOT NULL UNIQUE, -- application|transport|network|datalink|physical
  layer_name  TEXT NOT NULL,        -- 应用层|传输层|...
  main_function   TEXT NOT NULL,    -- 主要功能（Markdown 或纯文本）
  protocols       TEXT NOT NULL,    -- 常见协议，JSON 数组或逗号分隔
  devices_units   TEXT NOT NULL,    -- 设备/数据单位
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 6.3 表：各层知识点表（结构相同）

表名约定：

| layer_key | 表名 |
|-----------|------|
| application | `knowledge_application` |
| transport | `knowledge_transport` |
| network | `knowledge_network` |
| datalink | `knowledge_datalink` |
| physical | `knowledge_physical` |

```sql
-- 以 application 层为例，其余四层结构一致
CREATE TABLE knowledge_application (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  title       TEXT NOT NULL,
  content     TEXT NOT NULL,
  tags        TEXT DEFAULT '',
  sort_order  INTEGER DEFAULT 0,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_knowledge_application_title ON knowledge_application(title);
```

### 6.4 初始数据

- 提供 `server/db/schema.sql` 建表脚本
- 提供 `server/db/seed.sql` 每层至少 5 条示例知识点
- 提供 `server/db/network.db` 或在首次启动时自动初始化

---

## 7. 后端 API 接口文档

### 7.1 通用约定

| 项 | 说明 |
|----|------|
| Base URL | `http://localhost:3000/api` |
| 数据格式 | JSON，`Content-Type: application/json; charset=utf-8` |
| 字符编码 | UTF-8 |
| 时间格式 | ISO 8601，如 `2026-06-01T10:00:00.000Z` |

### 7.2 统一响应结构

**成功响应**

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

**失败响应**

```json
{
  "code": 40001,
  "message": "参数错误：title 不能为空",
  "data": null
}
```

**分页列表 `data` 结构**

```json
{
  "list": [],
  "pagination": {
    "page": 1,
    "pageSize": 10,
    "total": 56,
    "totalPages": 6
  }
}
```

### 7.3 错误码

| code | 含义 |
|------|------|
| 0 | 成功 |
| 40001 | 请求参数错误 |
| 40401 | 资源不存在 |
| 40402 | 分层 key 无效 |
| 50000 | 服务器内部错误 |

### 7.4 分层枚举 `layerKey`

| layerKey | 中文名 | 知识点表 |
|----------|--------|----------|
| `application` | 应用层 | knowledge_application |
| `transport` | 传输层 | knowledge_transport |
| `network` | 网络层 | knowledge_network |
| `datalink` | 数据链路层 | knowledge_datalink |
| `physical` | 物理层 | knowledge_physical |

---

### 7.5 接口清单

#### 7.5.1 健康检查

```
GET /api/health
```

**响应示例**

```json
{
  "code": 0,
  "message": "ok",
  "data": { "status": "up", "timestamp": "2026-06-01T08:00:00.000Z" }
}
```

---

#### 7.5.2 获取所有分层元数据

```
GET /api/layers
```

**响应 `data`**

```json
[
  {
    "id": 1,
    "layerKey": "application",
    "layerName": "应用层",
    "mainFunction": "为应用程序提供网络服务接口...",
    "protocols": ["HTTP", "DNS", "FTP", "SMTP"],
    "devicesUnits": ["报文 Message"],
    "updatedAt": "2026-06-01T08:00:00.000Z"
  }
]
```

---

#### 7.5.3 获取单层元数据

```
GET /api/layers/:layerKey
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| layerKey | string | 是 | 见 7.4 分层枚举 |

**响应 `data`**：单条 `layer_info` 对象。

---

#### 7.5.4 更新单层元数据（可选，P1）

```
PUT /api/layers/:layerKey
```

**请求体**

```json
{
  "mainFunction": "本层主要功能描述",
  "protocols": ["HTTP", "DNS"],
  "devicesUnits": "报文"
}
```

---

#### 7.5.5 分页查询知识点列表

```
GET /api/layers/:layerKey/knowledge
```

**Query 参数**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| page | number | 否 | 1 | 页码，从 1 开始 |
| pageSize | number | 否 | 10 | 每页条数，最大 50 |
| keyword | string | 否 | — | 模糊匹配 title、content、tags |

**请求示例**

```
GET /api/layers/transport/knowledge?page=1&pageSize=10&keyword=TCP
```

**响应 `data`**

```json
{
  "list": [
    {
      "id": 3,
      "title": "TCP 三次握手",
      "content": "SYN → SYN+ACK → ACK...",
      "tags": "TCP,连接管理",
      "sortOrder": 10,
      "createdAt": "2026-06-01T08:00:00.000Z",
      "updatedAt": "2026-06-01T08:00:00.000Z"
    }
  ],
  "pagination": {
    "page": 1,
    "pageSize": 10,
    "total": 1,
    "totalPages": 1
  }
}
```

---

#### 7.5.6 获取知识点详情

```
GET /api/layers/:layerKey/knowledge/:id
```

**响应 `data`**：单条知识点对象。

---

#### 7.5.7 新增知识点

```
POST /api/layers/:layerKey/knowledge
```

**请求体**

```json
{
  "title": "UDP 协议",
  "content": "无连接、不可靠传输...",
  "tags": "UDP,传输层",
  "sortOrder": 0
}
```

| 字段 | 类型 | 必填 |
|------|------|------|
| title | string | 是 |
| content | string | 是 |
| tags | string | 否 |
| sortOrder | number | 否 |

**响应 `data`**：创建后的完整知识点（含 `id`）。

---

#### 7.5.8 更新知识点

```
PUT /api/layers/:layerKey/knowledge/:id
```

**请求体**（字段均可选，至少传一项）

```json
{
  "title": "更新后的标题",
  "content": "更新后的内容",
  "tags": "新标签",
  "sortOrder": 5
}
```

---

#### 7.5.9 删除知识点

```
DELETE /api/layers/:layerKey/knowledge/:id
```

**响应 `data`**

```json
{ "id": 3, "deleted": true }
```

---

#### 7.5.10 知识图谱聚合数据（可选便捷接口）

```
GET /api/knowledge-graph
```

**说明**：前端也可自行调用五层 `GET /knowledge` 聚合；此接口减少请求次数。

**响应 `data`**

```json
{
  "name": "计算机网络",
  "children": [
    {
      "name": "应用层",
      "layerKey": "application",
      "children": [
        { "id": 1, "name": "HTTP", "type": "knowledge", "content": "..." }
      ]
    }
  ]
}
```

---

## 8. 前端接口对接说明

### 8.1 HTTP 客户端

- 推荐使用 `fetch` 或 `axios` 封装 `src/api/client.js`
- 开发环境 Vite 代理：

```js
// vite.config.js
export default {
  server: {
    proxy: {
      '/api': { target: 'http://localhost:3000', changeOrigin: true }
    }
  }
}
```

### 8.2 前端 API 模块划分

```
src/api/
├── client.js          # 基础请求封装
├── layers.js          # 分层元数据
├── knowledge.js       # 知识点 CRUD
└── knowledgeGraph.js  # 图谱聚合
```

### 8.3 前端调用示例

```js
// src/api/knowledge.js
import { request } from './client'

export function listKnowledge(layerKey, { page = 1, pageSize = 10, keyword = '' } = {}) {
  const qs = new URLSearchParams({ page, pageSize, keyword })
  return request(`/layers/${layerKey}/knowledge?${qs}`)
}

export function createKnowledge(layerKey, body) {
  return request(`/layers/${layerKey}/knowledge`, { method: 'POST', body })
}
```

### 8.4 纯前端模块（无 API）

| 模块 | 数据方式 |
|------|----------|
| TCP 可视化 | 组件内步骤配置 + 状态机 |
| DNS 可视化 | 组件内步骤配置 |
| 综合场景 | `src/data/scenarioSteps.js` JSON 驱动 |

---

## 9. 项目目录结构（目标）

```
ClassDemo/
├── docs/
│   └── PRD.md                 # 本文档
├── src/                       # 前端
│   ├── api/
│   ├── components/
│   │   ├── layout/            # 导航、页头
│   │   ├── protocol/          # TCP、DNS 可视化
│   │   ├── scenario/          # 综合场景
│   │   └── knowledge/         # 五层页、图谱
│   ├── data/
│   │   └── scenarioSteps.js
│   ├── views/
│   ├── router/
│   ├── App.vue
│   └── main.js
├── server/                    # 后端
│   ├── db/
│   │   ├── schema.sql
│   │   ├── seed.sql
│   │   └── network.db
│   ├── routes/
│   │   ├── layers.js
│   │   └── knowledge.js
│   ├── app.js
│   └── package.json
├── README.md
└── package.json
```

---

## 10. 开发里程碑（对齐实习安排）

| 日期 | 里程碑 | 交付物 |
|------|--------|--------|
| 6/1 | 需求分析与仓库 | PRD、GitHub 仓库、路由骨架 |
| 6/2 | 协议可视化 | TCP 完善 + DNS（或第二协议） |
| 6/5 | 综合场景 | `/scenario` 页面主体 |
| 6/8 | 知识体系 + DB | 后端 API、五层 Tab、CRUD |
| 6/9 | 整合 | 知识图谱、导航打通 |
| 6/12 | 提交 | README、设计方案、演示视频 |

---

## 11. 验收检查清单

### 11.1 第一部分

- [ ] 至少 2 个协议页面
- [ ] 拓扑 + 开始/下一步/重置
- [ ] 报文动画 + 状态/表格变化
- [ ] TCP 含建立连接、释放连接

### 11.2 第二部分

- [ ] t0→t1 步骤正确
- [ ] 每步含协议、MAC、IP、广播/单播
- [ ] ARP 表、交换表动态更新
- [ ] 路径高亮 + 最终结论

### 11.3 第三部分

- [ ] 五层 + 知识图谱共 6 个 Tab
- [ ] 知识点存数据库
- [ ] 增删改查 + 模糊查询 + 分页
- [ ] 刷新后数据不丢失

### 11.4 工程

- [ ] 首页导航
- [ ] README 完整
- [ ] schema.sql / seed.sql
- [ ] Git 分阶段提交

---

## 12. 附录

### 12.1 前后端技术栈版本建议

| 依赖 | 版本 |
|------|------|
| Node.js | ≥ 18 |
| Vue | ^3.5 |
| Vite | ^6 |
| Express | ^4 |
| better-sqlite3 或 sql.js | 最新稳定版 |

### 12.2 参考资料

- 《网络实习》任务与指导书，2026 年 5 月
- RFC 793 — TCP
- RFC 1034/1035 — DNS

### 12.3 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2026-06-01 | 初稿，基于指导书与题目 2 选型 |
