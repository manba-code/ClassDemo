# 计算机网络知识体系交互式展示系统 — 产品需求文档（PRD）

| 文档版本 | v1.4 |
|---------|------|
| 编写日期 | 2026-06-01 |
| 适用课程 | 《网络实习》 |
| 项目名称 | 计算机网络知识体系交互式展示系统 |
| 文档状态 | 已更新（第三部分后端 Java + MySQL） |

---

## 1. 文档说明

### 1.1 目的

本文档依据《网络实习》任务与指导书，将实习作业要求转化为可执行的产品需求、页面规格、数据模型与前后端接口约定，供开发、测试与验收使用。

### 1.2 范围

| 模块 | 是否需要后端 | 说明 |
|------|-------------|------|
| 第一部分：单项协议可视化 | 否 | 纯前端交互与动画 |
| 第二部分：综合网络场景模拟 | 否 | 纯前端分步演示 |
| 第三部分：知识体系与知识图谱 | 是 | **Java 后端** + 知识点 CRUD + 数据库持久化 |
| 系统导航与首页 | 否 | 路由与模块入口 |

### 1.3 已确认选型（本项目）

| 项 | 选型 |
|----|------|
| 第一部分协议 | **题目 2：TCP 三次握手与四次挥手**（✅ 已实现）+ **题目 5：路由转发**（✅ 已实现） |
| 前端 | Vue 3 + Vite + Vue Router + Tailwind CSS + lucide-vue-next 图标 |
| 后端（第三部分） | **Java 17+ / Spring Boot 3** + REST API（待开发） |
| 持久层 | **MyBatis-Plus** 或 Spring Data JPA（二选一，推荐 MyBatis-Plus） |
| 数据库 | **MySQL 8.x**（本地/演示）+ 建表 SQL 脚本（待开发） |
| 知识图谱 | 前端 JS 渲染（不要求图数据库存储，待开发） |

### 1.4 实现现状摘要（截至 v1.3）

| 模块 | 状态 | 关键文件 |
|------|------|----------|
| 首页 + 全局导航 | ✅ | `HomeView.vue`、`AppLayout.vue`、`router/index.js` |
| 题目 2 · TCP 握手/挥手 | ✅ | `TcpHandshakeVisualizer.vue` |
| 题目 5 · 路由转发 | ✅ | `RoutingVisualizer.vue`、`RoutingTablePanel.vue` |
| 综合场景模拟 | 待开发 | — |
| 知识体系 + Java 后端 | 待开发 | `backend/` Spring Boot 工程 |

两个协议页面均采用 **左栏拓扑与控制 + 右栏步骤详情/日志** 的统一布局，满足 COM-10～COM-15 交互规范。

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

- 至少 2 个协议可视化页面可正常运行。→ **已达成**（TCP + 路由转发）
- 综合场景步骤拆解正确，ARP 表与交换表动态更新。→ 待开发
- 五层模型 6 个子标签页可用，知识点修改后刷新不丢失。→ 待开发
- 具备首页导航，模块间可跳转。→ **已达成**（首页 + 顶部导航 + 协议子路由）
- README、建表脚本、演示截图齐全。→ 部分完成

---

## 3. 信息架构与页面地图

```
/                           首页（系统介绍 + 模块导航卡片）
├── /protocol               协议可视化（顶部下拉 + 移动端 Tab）
│   ├── /protocol/tcp       题目2：TCP 三次握手 / 四次挥手 ✅
│   └── /protocol/routing   题目5：路由转发 ✅
├── /scenario               第二部分：综合网络场景模拟（待开发）
└── /knowledge              第三部分：知识体系（待开发）
    ├── /knowledge/application   应用层
    ├── /knowledge/transport     传输层
    ├── /knowledge/network       网络层
    ├── /knowledge/datalink      数据链路层
    ├── /knowledge/physical      物理层
    ├── /knowledge/graph         知识图谱
    └── /knowledge/search        知识检索（EXT-04，可选）
```

---

## 4. 功能需求

### 4.1 公共模块

#### 4.1.1 首页 / 导航

| 编号 | 需求 | 优先级 |
|------|------|--------|
| COM-01 | 展示系统名称、简介、各模块入口卡片（含完成/待开发状态） | P0 |
| COM-02 | 顶部全局导航，支持首页与协议可视化子页面跳转 | P0 |
| COM-03 | 当前路由高亮，支持浏览器前进/后退 | P1 |
| COM-04 | 响应式布局，1280px 及以上为桌面优先 | P2 |

#### 4.1.2 通用交互规范（协议类页面）

所有协议可视化页面须满足：

| 编号 | 需求 | 优先级 |
|------|------|--------|
| COM-10 | 简洁网络拓扑图 | P0 |
| COM-11 | 「开始 / 下一步 / 重置」控制按钮（TCP 页为「建立连接 / 下一步 / 释放连接 / 重置」） | P0 |
| COM-12 | 协议执行步骤列表或时间线 | P0 |
| COM-13 | 报文/数据帧在拓扑中的传递动画 | P0 |
| COM-14 | 关键状态表或结果面板 | P0 |
| COM-15 | 禁止仅静态文字/图片，必须有动态交互 | P0 |

---

### 4.2 第一部分：单项协议原理可视化（20 分）

#### 4.2.1 题目 2 — TCP 三次握手与四次挥手（✅ 已实现）

**路由**：`/protocol/tcp`  
**组件**：`TcpView.vue` → `TcpHandshakeVisualizer.vue`

| 编号 | 需求 | 优先级 | 实现状态 |
|------|------|--------|----------|
| TCP-01 | 展示 Client、Server 两端节点 | P0 | ✅ 拓扑标注角色、MAC、IP |
| TCP-02 | 提供「建立连接」「释放连接」按钮 | P0 | ✅ 文案与指导书一致 |
| TCP-03 | 提供「下一步/重置」分步控制 | P0 | ✅ 握手 4 步 + 挥手 6 步可单步推进 |
| TCP-04 | 动画展示 SYN、SYN+ACK、ACK、FIN 报文传递 | P0 | ✅ C→S / S→C 方向动画 |
| TCP-05 | 实时展示 TCP 状态机 | P0 | ✅ 节点徽章 + 状态对照表 |
| TCP-06 | 展示 seq/ack 及 ISN | P1 | ✅ 报文详情面板 + 拓扑下方 ISN 提示 |
| TCP-07 | 握手/挥手完成后展示最终结果摘要 | P0 | ✅ 「连接已建立」「连接已关闭」横幅 |
| TCP-08 | 报文详情含源/目的 MAC、IP | P2 | ✅ 已实现 |
| TCP-09 | 协议执行步骤流日志 | P1 | ✅ 右侧步骤列表，最新步高亮 |
| TCP-10 | 当前阶段进度条 | P2 | ✅ 握手/挥手各阶段进度可视化 |

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

**固定地址参数（实现值）**

| 端 | MAC | IP |
|----|-----|-----|
| Client | 00:1A:2B:3C:4D:5E | 192.168.1.100:52431 |
| Server | 00:AA:BB:CC:DD:EE | 203.0.113.50:443 |

**步骤划分（实现值）**

| 阶段 | 步骤数 | 概要 |
|------|--------|------|
| 三次握手 | 4 步 | SYN → SYN+ACK → ACK → 双方 ESTABLISHED |
| 四次挥手 | 6 步 | FIN → ACK → FIN-WAIT-2 → FIN → ACK → 双方 CLOSED |

---

#### 4.2.2 题目 5 — 路由转发（✅ 已实现）

**路由**：`/protocol/routing`  
**组件**：`RoutingView.vue` → `RoutingVisualizer.vue` + `RoutingTablePanel.vue`

| 编号 | 需求 | 优先级 | 实现状态 |
|------|------|--------|----------|
| ROUT-01 | 展示跨网段拓扑：H1、R1、R2、H2 及三段网段 | P0 | ✅ 三网段标注 192.168.1.0/24、10.0.0.0/24、172.16.0.0/24 |
| ROUT-02 | 提供「开始 / 下一步 / 重置」分步控制 | P0 | ✅ 共 6 步可单步推进 |
| ROUT-03 | IP 数据报在链路上的传递动画 | P0 | ✅ 链路高亮 + 报文气泡动画 |
| ROUT-04 | 展示 R1、R2 路由表，查表时高亮匹配行 | P0 | ✅ `RoutingTablePanel` 动态高亮 |
| ROUT-05 | 每步展示处理设备、查表结果、下一跳、出接口 | P0 | ✅ 右侧「当前步骤详情」面板 |
| ROUT-06 | 转发步骤日志列表 | P1 | ✅ 右侧「转发步骤」时间线 |
| ROUT-07 | 转发完成后展示路径摘要 | P0 | ✅ 「转发完成」结果横幅 |
| ROUT-08 | 当前处理节点/链路视觉反馈 | P1 | ✅ 节点边框、链路颜色随步骤变化 |

**拓扑与地址规划（实现值）**

| 设备 | IP | 说明 |
|------|-----|------|
| H1 | 192.168.1.10 | 源主机，网段 A |
| R1 | 192.168.1.1 / 10.0.0.1 | 连接网段 A 与 B |
| R2 | 10.0.0.2 / 172.16.0.1 | 连接网段 B 与 C |
| H2 | 172.16.0.20 | 目的主机，网段 C |

**R1 路由表（实现值）**

| 目的网络 | 掩码 | 下一跳 | 接口 |
|----------|------|--------|------|
| 192.168.1.0 | /24 | 直连 | eth0 |
| 10.0.0.0 | /24 | 直连 | eth1 |
| 172.16.0.0 | /24 | 10.0.0.2 | eth1 |

**R2 路由表（实现值）**

| 目的网络 | 掩码 | 下一跳 | 接口 |
|----------|------|--------|------|
| 172.16.0.0 | /24 | 直连 | eth1 |
| 10.0.0.0 | /24 | 直连 | eth0 |
| 192.168.1.0 | /24 | 10.0.0.1 | eth0 |

**参考拓扑**

```
网段 A                网段 B                网段 C
192.168.1.0/24       10.0.0.0/24          172.16.0.0/24

[H1] —— [R1] —— [R2] —— [H2]
 .10    .1/.1      .2/.1    .20
```

**步骤划分（实现值，共 6 步）**

| 步骤 | 名称 | 概要 |
|------|------|------|
| 1 | H1 发送数据报 | 目的不在本地网段，发往默认网关 R1 |
| 2 | R1 查路由表 | 匹配 172.16.0.0/24，下一跳 10.0.0.2 |
| 3 | R1 → R2 转发 | 经网段 B 送达 R2 |
| 4 | R2 查路由表 | 匹配直连网段 172.16.0.0/24 |
| 5 | R2 → H2 转发 | 经网段 C 送达 H2 |
| 6 | H2 接收数据报 | 转发过程结束 |

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
**后端**：Java Spring Boot，独立工程目录 `backend/`，默认端口 `8080`

#### 4.4.0 后端实现约定

| 编号 | 需求 | 优先级 |
|------|------|--------|
| BE-01 | Spring Boot 提供 REST API，路径前缀 `/api` | P0 |
| BE-02 | 统一 JSON 响应包装（`code` / `message` / `data`） | P0 |
| BE-03 | 全局异常处理，参数校验失败返回 40001 | P0 |
| BE-04 | 配置 CORS，允许 Vue 开发服务器（`localhost:5173`）跨域访问 | P0 |
| BE-05 | 应用启动时自动执行 `schema.sql`、`seed.sql` 初始化 MySQL（需预先创建空库） | P0 |
| BE-06 | Controller / Service / Mapper 分层，五层知识点分表访问 | P0 |
| BE-07 | 本地演示无登录鉴权 | — |

**推荐包结构**：`com.classdemo.network.{controller, service, mapper, entity, dto, config, common}`

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
| KN-09 | 修改持久化，刷新不丢失 | P0 | MySQL（Java 后端读写） |

**知识点字段**

| 字段 | 类型（MySQL） | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT AUTO_INCREMENT | 自动 | 主键 |
| title | VARCHAR(255) | 是 | 知识点标题 |
| content | TEXT | 是 | 详细内容 |
| tags | VARCHAR(512) | 否 | 标签，逗号分隔 |
| sort_order | INT | 否 | 排序权重，默认 0 |
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

#### 4.5.0 总览

| 编号 | 功能 | 主要落点 | 后端 | 建议分值 |
|------|------|----------|------|----------|
| EXT-01 | 网络故障模拟 | `/scenario`（主）、`/protocol/tcp`（辅） | 否 | 1～2 分 |
| EXT-02 | 协议过程回放 | `/protocol/*`、`/scenario` | 否 | 1 分 |
| EXT-03 | 报文结构解析 | 协议页 + 综合场景（共享组件） | 否 | 1～2 分 |
| EXT-04 | 知识检索问答 | `/knowledge/search` 或知识体系顶栏入口 | 是 | 1 分 |

**设计原则**

1. **教学优先**：每项须能向学生解释「发生了什么、为何失败/成功」，禁止仅换皮 UI。
2. **复用现有能力**：回放与单步建立在 COM-11、SCN-02 已有「下一步/重置」之上扩展；报文解析复用各页 `buildDetail` / 步骤 JSON 中的地址与标志位。
3. **分阶段交付**：可先实现「正常路径 + 一种故障 + TCP 页回放 + 单层报文解析 + 全库 keyword 检索」，再补全其余组合。

**推荐实现顺序**：EXT-03（与 TCP/场景同步）→ EXT-02 → EXT-01 → EXT-04。

---

#### 4.5.1 EXT-01 — 网络故障模拟

**目标**：在综合场景与 TCP 演示中，通过可切换的「故障模式」展示常见网络异常的表现与排查思路，与正常流程形成对比。

**路由与入口**

| 编号 | 需求 | 优先级 |
|------|------|--------|
| EXT-01-01 | 综合场景页 `/scenario` 提供「运行模式」：`正常` / `DNS 失败` / `ARP 错误` / `TCP 超时` | P0 |
| EXT-01-02 | 模式在点击「开始」前选定；运行中禁用切换，「重置」后恢复可选 | P0 |
| EXT-01-03 | TCP 页 `/protocol/tcp` 可选增加「SYN 无响应（超时）」演示模式（与握手流程二选一或独立 Tab） | P2 |

**故障类型定义**

| 模式 | 触发步骤（参考） | 现象（须可视化） | 教学说明（结果面板） |
|------|------------------|------------------|----------------------|
| DNS 失败 | DNS 查询步（场景约第 4～5 步） | 本地 DNS 返回失败或无 A 记录；后续 TCP/HTTP 不执行 | 说明域名无法解析，浏览器无法获得目的 IP |
| ARP 错误 | 网关 ARP 步（场景约第 2～3 步） | 无 Reply、错误 MAC 或超时；H1 ARP 表不更新或写入错误项 | 说明二层无法送达网关，三层报文无法发出 |
| TCP 超时 | 向 Web Server 发 SYN 步（场景约第 6 步或 TCP 页握手第 1 步） | Server 不响应；展示重传 1～2 次（动画可简化）后失败 | 说明连接建立失败、可能原因（路由、防火墙、服务未监听） |

| 编号 | 需求 | 优先级 |
|------|------|--------|
| EXT-01-04 | 故障模式下拓扑路径以**红色/虚线**标示失败方向，正常步仍为默认高亮样式 | P0 |
| EXT-01-05 | 右侧步骤区展示**故障码/原因**（如 `NXDOMAIN`、`ARP_TIMEOUT`、`TCP_RST` 或文案等价物） | P0 |
| EXT-01-06 | 流程在故障点**终止**或进入「失败结论」步，禁止静默继续正常 HTTP 成功路径 | P0 |
| EXT-01-07 | 提供「与正常模式对比」摘要：列出正常模式下本步应发生的行为（可折叠面板） | P1 |
| EXT-01-08 | 故障分支数据由前端 JSON 配置（`faultProfiles`），与 `SCN-10` 一致，无需后端 | P1 |

**数据配置约定（实现参考）**

```json
{
  "mode": "dns_fail",
  "label": "DNS 解析失败",
  "branchFromStep": 4,
  "steps": [ "... 替代第 4 步及之后 ..." ],
  "conclusion": "H1 无法获得 www.abc.com 的 IP，访问终止。"
}
```

---

#### 4.5.2 EXT-02 — 协议过程回放

**目标**：支持演示过程中的**暂停、回退、从头回放**与**播放速度**，便于课堂讲解与反复观察某一报文。

**适用页面**

| 页面 | 路由 | 优先级 |
|------|------|--------|
| TCP 握手/挥手 | `/protocol/tcp` | P0 |
| 路由转发 | `/protocol/routing` | P0 |
| 综合场景 | `/scenario` | P1 |

| 编号 | 需求 | 优先级 |
|------|------|--------|
| EXT-02-01 | 控制栏包含：`上一步` / `下一步` / `暂停·继续` / `从头回放` / `重置`（与 COM-11、SCN-02 对齐） | P0 |
| EXT-02-02 | **暂停**：报文动画进行中可冻结在当前进度；**继续**从冻结点恢复 | P0 |
| EXT-02-03 | **上一步**：回退到上一逻辑步，恢复该步开始前的拓扑/表/状态机快照（须维护 `stepHistory`） | P0 |
| EXT-02-04 | **从头回放**：从第 0 步重新自动或手动播放，不清空用户所选模式（如 EXT-01 故障模式） | P0 |
| EXT-02-05 | 步骤时间线支持**点击已执行步骤**跳转回放（跳步时状态与表须与目标步一致） | P1 |
| EXT-02-06 | 播放速度：`0.5x` / `1x` / `2x`，仅影响动画时长，不改变协议语义 | P2 |
| EXT-02-07 | 运行中禁用冲突操作（如跳步与「下一步」并发），按钮 `disabled` 状态明确 | P1 |
| EXT-02-08 | 键盘快捷键（可选）：`Space` 暂停/继续，`→` 下一步，`←` 上一步 | P2 |

**状态模型（前端）**

```
currentStepIndex, isPlaying, isPaused, playbackSpeed,
stepHistory: Array<{ index, arpTable, macTable, tcpState, ... }>
```

**共享组件建议**：`src/components/PlaybackControls.vue`，由三个 Visualizer 引用。

---

#### 4.5.3 EXT-03 — 报文结构解析

**目标**：将当前步骤涉及的报文按 **Ethernet → IP → TCP/UDP → 应用层** 分层展示字段含义，强化「封装」概念；与右侧「报文详情」互补，详情偏摘要，本功能偏结构树。

| 编号 | 需求 | 优先级 |
|------|------|--------|
| EXT-03-01 | 提供可折叠面板「报文结构解析」，默认展示当前步对应报文；无报文步显示占位说明 | P0 |
| EXT-03-02 | 至少支持协议类型：**Ethernet、IPv4、TCP、UDP、HTTP**（按步骤出现情况渲染，不必每步五层齐全） | P0 |
| EXT-03-03 | 每层以**树形字段列表**展示：字段名、示例值、简短说明（中文） | P0 |
| EXT-03-04 | 当前步相对上一步**变化字段**高亮（如 seq/ack、TTL 递减、MAC 学习） | P1 |
| EXT-03-05 | TCP 页须含：源/目的端口、标志位 SYN/ACK/FIN、seq、ack、窗口（可固定示例值） | P0 |
| EXT-03-06 | 场景页 DNS 步含 UDP 首部；HTTP 步含请求行、`Host`、`GET` 路径 | P0 |
| EXT-03-07 | 路由转发页至少解析 **Ethernet + IPv4**（无传输层负载时标注「载荷：上层协议数据」） | P1 |
| EXT-03-08 | 字段数据来源于步骤配置或 `buildPacketLayers(step)` 纯函数，**不解析真实 pcap** | P0 |
| EXT-03-09 | 共享组件 `PacketStructurePanel.vue`，props：`layers: PacketLayer[]` | P0 |

**字段树示例（TCP SYN，节选）**

| 层级 | 字段 | 示例值 | 说明 |
|------|------|--------|------|
| Ethernet | Dst MAC | AA:AA:AA:AA:AA:FE | 下一跳网关 |
| Ethernet | Src MAC | AA:AA:AA:AA:AA:01 | H1 |
| Ethernet | Type | 0x0800 | IPv4 |
| IPv4 | Src IP | 192.168.1.10 | H1 |
| IPv4 | Dst IP | 203.0.113.80 | Web Server |
| TCP | Flags | SYN | 请求建立连接 |
| TCP | Seq | 1000 | 客户端 ISN |

---

#### 4.5.4 EXT-04 — 知识检索问答

**目标**：在第三部分提供**跨五层**的知识点检索，以「提问 → 匹配知识点 → 展示摘要与出处」的方式增强 `KN-08` 单层 keyword 查询，便于学生自助答疑。

**路由**：`/knowledge/search`（或在 `KnowledgeLayout` 顶栏常驻搜索框跳转至此页）

| 编号 | 需求 | 优先级 |
|------|------|--------|
| EXT-04-01 | 搜索框支持自然语言式问句（实际匹配仍为 keyword，不要求大模型） | P0 |
| EXT-04-02 | 后端提供 `GET /api/knowledge/search?keyword=&page=&pageSize=`，**五表联合**模糊匹配 `title`、`content`、`tags` | P0 |
| EXT-04-03 | 响应每条结果含：`layerKey`、`layerName`、`id`、`title`、`content` 摘要（截取前 200 字）、`tags`、相关度或排序字段 | P0 |
| EXT-04-04 | 结果列表按层分组或标签展示所属层；点击跳转对应层详情/编辑上下文（只读展示即可） | P0 |
| EXT-04-05 | 无结果时提示「未找到相关知识点」并给出示例关键词（如 TCP、ARP、DNS） | P0 |
| EXT-04-06 | 关键词在摘要中高亮显示（前端 `mark` 或包裹 `<mark>`） | P1 |
| EXT-04-07 | 可选「推荐问题」 chips：如「什么是三次握手」「ARP 作用是什么」，点击即发起搜索 | P1 |
| EXT-04-08 | 分页：`page` 从 1 开始，`pageSize` 默认 10、最大 20 | P1 |

**与 KN-08 的关系**

| 能力 | KN-08 | EXT-04 |
|------|-------|--------|
| 范围 | 单层 Tab 内列表 | 五层聚合 |
| 入口 | 各层页筛选框 | 独立检索页/全局搜索 |
| API | `GET /layers/:layerKey/knowledge?keyword=` | `GET /knowledge/search?keyword=` |

**「问答」呈现（前端模板，无需 AI）**

匹配到结果时，顶部展示：

> 根据知识点库，与「{keyword}」相关的内容如下（共 N 条）：

首条可展开完整 `content`；多条时列表展示。

---

#### 4.5.5 附加功能验收要点（答辩/自检）

| 检查项 | EXT |
|--------|-----|
| 至少 2 种故障模式可演示且流程正确终止 | EXT-01 |
| TCP 或场景页具备暂停 + 上一步 + 从头回放 | EXT-02 |
| 任一页出现分层报文字段树且含中文说明 | EXT-03 |
| 跨层搜索返回结果并可查看知识点摘要 | EXT-04 |

---

## 5. 非功能需求

| 编号 | 类别 | 要求 |
|------|------|------|
| NFR-01 | 兼容性 | Chrome / Edge 最新版正常 |
| NFR-02 | 性能 | 协议动画流畅，单页切换 < 500ms |
| NFR-03 | 可维护性 | 前后端分离，目录结构清晰 |
| NFR-04 | 安全 | 本地教学演示，无鉴权；生产环境不在范围 |
| NFR-05 | 部署 | 前端 `npm run dev`（5173）；本地 MySQL 8.x 已启动；Java 后端 `mvn spring-boot:run` 或 IDE 启动（8080）；提供 README |
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
  id              INT PRIMARY KEY,              -- 1~5 对应五层
  layer_key       VARCHAR(32) NOT NULL UNIQUE,    -- application|transport|network|datalink|physical
  layer_name      VARCHAR(64) NOT NULL,         -- 应用层|传输层|...
  main_function   TEXT NOT NULL,                  -- 主要功能（Markdown 或纯文本）
  protocols       TEXT NOT NULL,                  -- 常见协议，JSON 数组或逗号分隔
  devices_units   VARCHAR(512) NOT NULL,          -- 设备/数据单位
  updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
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
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(255) NOT NULL,
  content     TEXT NOT NULL,
  tags        VARCHAR(512) DEFAULT '',
  sort_order  INT DEFAULT 0,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_knowledge_application_title ON knowledge_application(title);
```

### 6.4 初始数据与环境准备

**MySQL 前置条件**

1. 安装 MySQL 8.x（本地或 Docker 均可）
2. 创建数据库与用户：执行 `backend/scripts/init-mysql.sql`，或手动执行以下 SQL：

```sql
CREATE DATABASE classdemo_network DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'classdemo'@'localhost' IDENTIFIED BY 'classdemo123';
GRANT ALL PRIVILEGES ON classdemo_network.* TO 'classdemo'@'localhost';
FLUSH PRIVILEGES;
```

**脚本与初始化**

- 提供 `backend/src/main/resources/db/schema.sql` 建表脚本（MySQL 语法）
- 提供 `backend/src/main/resources/db/seed.sql` 每层至少 5 条示例知识点
- 首次启动时由 Spring Boot `spring.sql.init.*` 自动执行 schema/seed（需目标库已存在且为空）

**Spring Boot 配置示例（`application.yml`）**

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/classdemo_network?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: classdemo
    password: classdemo123
  sql:
    init:
      mode: always
      schema-locations: classpath:db/schema.sql
      data-locations: classpath:db/seed.sql
```

> 生产或多人协作时，可将 `spring.sql.init.mode` 改为 `never`，改用手动执行 SQL 或 Flyway/Liquibase 管理迁移。

---

## 7. 后端 API 接口文档

> **实现语言**：Java（Spring Boot 3）。以下接口约定供前后端联调；Controller 返回统一包装类 `ApiResponse<T>`。

### 7.1 通用约定

| 项 | 说明 |
|----|------|
| 实现框架 | Spring Boot 3 + Spring Web |
| Base URL | `http://localhost:8080/api` |
| 数据格式 | JSON，`Content-Type: application/json; charset=utf-8` |
| 字符编码 | UTF-8 |
| 时间格式 | ISO 8601，如 `2026-06-01T10:00:00.000Z` |
| 跨域 | 开发环境由 `@CrossOrigin` 或 `WebMvcConfigurer` 放行 `http://localhost:5173` |

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

**Java 统一响应类（参考）**

```java
public record ApiResponse<T>(int code, String message, T data) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
```

### 7.4 分层枚举 `layerKey`

| layerKey | 中文名 | 知识点表 |
|----------|--------|----------|
| `application` | 应用层 | knowledge_application |
| `transport` | 传输层 | knowledge_transport |
| `network` | 网络层 | knowledge_network |
| `datalink` | 数据链路层 | knowledge_datalink |
| `physical` | 物理层 | knowledge_physical |

### 7.5 Spring Boot Controller 映射（参考）

| HTTP | 路径 | Controller 方法 | 说明 |
|------|------|-----------------|------|
| GET | `/api/health` | `HealthController.check()` | 健康检查 |
| GET | `/api/layers` | `LayerController.listAll()` | 五层元数据列表 |
| GET | `/api/layers/{layerKey}` | `LayerController.getOne()` | 单层元数据 |
| PUT | `/api/layers/{layerKey}` | `LayerController.update()` | 更新元数据（P1） |
| GET | `/api/layers/{layerKey}/knowledge` | `KnowledgeController.list()` | 分页 + keyword |
| GET | `/api/layers/{layerKey}/knowledge/{id}` | `KnowledgeController.getById()` | 详情 |
| POST | `/api/layers/{layerKey}/knowledge` | `KnowledgeController.create()` | 新增 |
| PUT | `/api/layers/{layerKey}/knowledge/{id}` | `KnowledgeController.update()` | 更新 |
| DELETE | `/api/layers/{layerKey}/knowledge/{id}` | `KnowledgeController.delete()` | 删除 |
| GET | `/api/knowledge-graph` | `KnowledgeGraphController.aggregate()` | 图谱聚合（可选） |

> `layerKey` 无效时抛出业务异常，由 `GlobalExceptionHandler` 转换为 `40402`。

---

### 7.6 接口清单

#### 7.6.1 健康检查

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

#### 7.6.2 获取所有分层元数据

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

#### 7.6.3 获取单层元数据

```
GET /api/layers/:layerKey
```

**路径参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| layerKey | string | 是 | 见 7.4 分层枚举 |

**响应 `data`**：单条 `layer_info` 对象。

---

#### 7.6.4 更新单层元数据（可选，P1）

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

#### 7.6.5 分页查询知识点列表

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

#### 7.6.6 获取知识点详情

```
GET /api/layers/:layerKey/knowledge/:id
```

**响应 `data`**：单条知识点对象。

---

#### 7.6.7 新增知识点

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

#### 7.6.8 更新知识点

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

#### 7.6.9 删除知识点

```
DELETE /api/layers/:layerKey/knowledge/:id
```

**响应 `data`**

```json
{ "id": 3, "deleted": true }
```

---

#### 7.6.10 知识图谱聚合数据（可选便捷接口）

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

#### 7.6.11 跨层知识检索（EXT-04）

```
GET /api/knowledge/search
```

**Query 参数**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| keyword | string | 是 | — | 匹配五张 `knowledge_*` 表的 title、content、tags |
| page | number | 否 | 1 | 页码 |
| pageSize | number | 否 | 10 | 每页条数，最大 20 |

**请求示例**

```
GET /api/knowledge/search?keyword=三次握手&page=1&pageSize=10
```

**响应 `data`**

```json
{
  "keyword": "三次握手",
  "list": [
    {
      "layerKey": "transport",
      "layerName": "传输层",
      "id": 3,
      "title": "TCP 三次握手",
      "snippet": "SYN → SYN+ACK → ACK...",
      "tags": "TCP,连接管理",
      "sortOrder": 10
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

**错误**：`keyword` 为空或仅空白时返回 `40001`，message 如「关键词不能为空」。

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
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
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
├── knowledgeGraph.js  # 图谱聚合
└── knowledgeSearch.js # 跨层检索（EXT-04）

src/components/        # 附加功能共享组件（建议）
├── PlaybackControls.vue      # EXT-02
└── PacketStructurePanel.vue  # EXT-03
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

| 模块 | 路由 | 数据方式 | 状态 |
|------|------|----------|------|
| TCP 可视化 | `/protocol/tcp` | `TcpHandshakeVisualizer.vue` 内 `handshakeSteps` / `waveSteps` + 状态机 | ✅ 已实现 |
| 路由转发 | `/protocol/routing` | `RoutingVisualizer.vue` 内 `steps` + 路由表常量 | ✅ 已实现 |
| 综合场景 | `/scenario` | `src/data/scenarioSteps.js` JSON 驱动（规划） | 待开发 |

---

## 9. 项目目录结构（目标）

```
ClassDemo/
├── docs/
│   └── PRD.md                      # 本文档
├── src/                            # 前端
│   ├── components/
│   │   ├── TcpHandshakeVisualizer.vue   # 题目2 TCP 可视化 ✅
│   │   ├── RoutingVisualizer.vue        # 题目5 路由转发 ✅
│   │   └── RoutingTablePanel.vue        # 路由表面板 ✅
│   ├── layouts/
│   │   └── AppLayout.vue                # 顶部导航 + 协议下拉 ✅
│   ├── views/
│   │   ├── HomeView.vue                 # 首页模块卡片 ✅
│   │   └── protocol/
│   │       ├── TcpView.vue              # ✅
│   │       └── RoutingView.vue          # ✅
│   ├── router/
│   │   └── index.js                     # 路由配置 ✅
│   ├── api/                             # 第三部分 API 封装（待开发）
│   ├── data/
│   │   └── scenarioSteps.js             # 综合场景（待开发）
│   ├── App.vue
│   └── main.js
├── backend/                         # Java 后端（第三部分，待开发）
│   ├── pom.xml                      # Maven 依赖：Spring Boot、MySQL Connector/J、MyBatis-Plus
│   ├── scripts/
│   │   └── init-mysql.sql           # MySQL 建库建用户脚本
│   └── src/main/
│       ├── java/com/classdemo/network/
│       │   ├── NetworkApplication.java
│       │   ├── controller/          # LayerController、KnowledgeController
│       │   ├── service/
│       │   ├── mapper/
│       │   ├── entity/
│       │   ├── dto/
│       │   ├── config/              # CorsConfig、MyBatisConfig
│       │   └── common/              # ApiResponse、GlobalExceptionHandler
│       └── resources/
│           ├── application.yml
│           └── db/
│               ├── schema.sql
│               └── seed.sql
├── README.md
└── package.json
```

---

## 10. 开发里程碑（对齐实习安排）

| 日期 | 里程碑 | 交付物 |
|------|--------|--------|
| 6/1 | 需求分析与仓库 | PRD、GitHub 仓库、路由骨架 ✅ |
| 6/2 | 协议可视化 | TCP 完善 + 路由转发 ✅ |
| 6/5 | 综合场景 | `/scenario` 页面主体 |
| 6/8 | 知识体系 + DB | Java Spring Boot API、五层 Tab、CRUD |
| 6/9 | 整合 | 知识图谱、导航打通 |
| 6/10 | 附加功能（可选） | EXT-03 报文解析 → EXT-02 回放 → EXT-01 故障 → EXT-04 检索 |
| 6/12 | 提交 | README、设计方案、演示视频 |

---

## 11. 验收检查清单

### 11.1 第一部分

- [x] 至少 2 个协议页面（TCP + 路由转发）
- [x] 拓扑 + 开始/下一步/重置
- [x] 报文/数据包动画 + 状态/表格变化
- [x] TCP 含建立连接、释放连接
- [x] 路由转发含路由表查表高亮与路径摘要

### 11.2 第二部分

- [ ] t0→t1 步骤正确
- [ ] 每步含协议、MAC、IP、广播/单播
- [ ] ARP 表、交换表动态更新
- [ ] 路径高亮 + 最终结论

### 11.3 第三部分

- [ ] 五层 + 知识图谱共 6 个 Tab
- [ ] Java Spring Boot 后端可独立启动（8080）
- [ ] 知识点存 MySQL 数据库
- [ ] 增删改查 + 模糊查询 + 分页
- [ ] 刷新后数据不丢失
- [ ] 前端 Vite 代理 `/api` → 8080 联调通过

### 11.4 工程

- [x] 首页导航（含协议可视化下拉与子路由）
- [ ] README 完整
- [ ] `backend/src/main/resources/db/schema.sql` / `seed.sql`
- [ ] Git 分阶段提交

### 11.5 附加功能（加分项，可选）

- [ ] EXT-01：综合场景 ≥2 种故障模式，故障点终止且有必要说明
- [ ] EXT-02：TCP 或场景页支持暂停、上一步、从头回放
- [ ] EXT-03：分层报文结构树（含 Ethernet / IP / TCP 或 UDP / HTTP 中至少 3 类）
- [ ] EXT-04：`GET /api/knowledge/search` 可用，检索页展示跨层结果与摘要

---

## 12. 附录

### 12.1 前后端技术栈版本建议

**前端**

| 依赖 | 版本 |
|------|------|
| Node.js | ≥ 18（仅前端构建与 dev server） |
| Vue | ^3.5 |
| Vite | ^6 |

**后端（第三部分）**

| 依赖 | 版本 |
|------|------|
| JDK | 17 或 21（LTS） |
| Spring Boot | ^3.2 |
| MyBatis-Plus | ^3.5（或 Spring Data JPA） |
| MySQL Connector/J | 最新稳定版（`com.mysql:mysql-connector-j`） |
| Maven | ≥ 3.8 |

### 12.2 参考资料

- 《网络实习》任务与指导书，2026 年 5 月
- RFC 793 — TCP
- RFC 1034/1035 — DNS

### 12.3 修订记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0 | 2026-06-01 | 初稿，基于指导书与题目 2 选型 |
| v1.1 | 2026-06-01 | 对齐已实现页面：题目 2 TCP、题目 5 路由转发；更新信息架构、目录结构与验收清单 |
| v1.2 | 2026-06-01 | 第三部分后端选型改为 Java Spring Boot；更新 API 端口、目录结构、部署与验收说明 |
| v1.3 | 2026-06-02 | 第三部分持久层选型改为 MySQL 8.x；更新建表 SQL、数据源配置与验收说明 |
| v1.4 | 2026-06-02 | 扩展 §4.5 附加功能需求（EXT-01～04）、§7.6.11 检索 API、§11.5 验收与实现顺序 |
