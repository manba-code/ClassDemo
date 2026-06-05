# 计算机网络知识体系交互式展示系统

## 1. 项目简介

面向《网络实习》课程的 **Web 交互式教学系统**，集协议过程可视化、综合通信场景模拟、TCP/IP 五层知识体系管理与知识图谱展示于一体。学生可通过分步动画观察报文传递、路由查表、ARP/DNS/TCP 握手等过程，并在知识体系模块中维护与检索各层知识点。

| 项 | 说明 |
|----|------|
| 适用课程 | 《网络实习》 |
| 详细需求 | 见 [docs/PRD.md](docs/PRD.md) |

---

## 2. 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | Vue 3、Vite 6、Vue Router 4、Tailwind CSS 3、lucide-vue-next、ECharts 6 |
| 后端 | Java 17、Spring Boot 3.2、MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.x |
| 部署 | 前端 dev 端口 `5173`；后端 REST 前缀 `/api`，端口 `8080` |

---

## 3. 环境配置

### 3.1 软件版本要求

| 组件 | 版本 |
|------|------|
| Node.js | 18+（推荐 LTS） |
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.x（或 Docker） |

### 3.2 数据库

**方式一：Docker（推荐）**

```bash
docker compose -f backend/docker-compose.yml up -d
```

默认：库名 `classdemo_network`，用户 `classdemo` / 密码 `classdemo123`，端口 `3306`。

**方式二：本机 MySQL**

```bash
mysql -u root -p < backend/scripts/init-mysql.sql
```

账号需与 `backend/src/main/resources/application.yml` 中 `spring.datasource` 一致。

### 3.3 应用配置

| 文件 | 说明 |
|------|------|
| `backend/src/main/resources/application.yml` | MySQL 连接、端口、自动建表/种子数据 |
| `vite.config.js` | 前端 `/api` 代理至 `http://localhost:8080` |

若中文显示乱码，执行 `backend/scripts/fix-encoding-reseed.sql` 修复字符集后重新导入种子数据。

---

## 4. 安装依赖

### 前端

在项目根目录：

```bash
npm install
```

### 后端

Maven 会在首次 `spring-boot:run` 或 `mvn package` 时自动下载依赖，无需单独安装步骤。

---

## 5. 启动运行

### 5.1 启动后端

```bash
cd backend
mvn spring-boot:run
```

- 服务地址：`http://localhost:8080/api`
- 健康检查：`GET http://localhost:8080/api/health`

首次启动自动执行 `schema.sql`、`seed.sql` 建表并导入种子数据。验证示例：

```bash
mysql -u classdemo -pclassdemo123 classdemo_network -e "SHOW TABLES; SELECT COUNT(*) FROM layer_info;"
```

应看到 6 张表，`layer_info` 5 行，各 `knowledge_*` 表有初始数据。

### 5.2 启动前端

在项目根目录：

```bash
npm run dev
```

浏览器访问：`http://localhost:5173`

### 5.3 生产构建（可选）

```bash
npm run build
npm run preview
```

---

## 6. 主要功能

### 6.1 核心模块

| 模块 | 路由 | 说明 |
|------|------|------|
| 首页导航 | `/` | 系统介绍与各模块入口 |
| TCP 协议 | `/protocol/tcp` | 三次握手 / 四次挥手，报文动画与 TCP 状态机 |
| 路由转发 | `/protocol/routing` | 跨网段 IP 转发、路由表查表与分步演示 |
| 综合场景 | `/scenario` | H1 访问 `www.abc.com`：ARP → DNS → TCP → HTTP 全流程 |
| 知识体系 | `/knowledge/{layerKey}` | 五层 Tab 知识点 CRUD、单层 keyword 检索 |
| 知识图谱 | `/knowledge/graph` | 五层 + 知识点树形结构（ECharts） |
| 知识检索 | `/knowledge/search` | 跨五层联合检索与问答式展示（需后端） |

协议类页面采用 **左栏拓扑与控制 + 右栏步骤详情/日志** 布局，支持分步交互与报文传递动画。

### 6.2 附加功能（PRD §4.5，加分项）

以下四项为课程附加功能，均已实现。

#### EXT-01 网络故障模拟

**入口**：`/scenario` 页顶部「运行模式」

| 模式 | 触发点 | 现象 | 故障码 |
|------|--------|------|--------|
| DNS 失败 | DNS 查询步 | 返回 NXDOMAIN，后续 TCP/HTTP 不执行 | `NXDOMAIN` |
| ARP 错误 | 网关 ARP 步 | 无 Reply / 超时，ARP 表不更新 | `ARP_TIMEOUT` |
| TCP 超时 | 向 Web Server 发 SYN | Server 无响应，展示 SYN 重传后失败 | `TCP_TIMEOUT` |

- 运行前选定模式；运行中不可切换，「重置」后恢复可选
- 故障路径以红色高亮，右侧展示故障码与教学说明
- 流程在故障点终止，并提供「与正常模式对比」折叠面板
- 配置数据：`src/data/faultProfiles.js`（纯前端 JSON，无需后端）

**TCP 页辅助演示**：`/protocol/tcp` 可选「SYN 无响应（超时）」模式，展示 SYN 重传与连接失败。

#### EXT-02 协议过程回放

**入口**：TCP / 路由转发 / 综合场景 控制栏

| 能力 | 说明 |
|------|------|
| 上一步 | 回退至上一逻辑步，恢复拓扑/表/状态机快照（`stepHistory`） |
| 下一步 | 单步推进（原有能力） |
| 暂停 / 继续 | 报文动画进行中可冻结与恢复 |
| 从头回放 | 从第 0 步重播，保留当前故障模式等用户选择 |
| 重置 | 清空进度回到空闲 |
| 播放速度 | `0.5x` / `1x` / `2x`，仅影响动画时长 |
| 快捷键 | `Space` 暂停/继续，`←` `→` 上/下一步 |

共享组件：`src/components/PlaybackControls.vue`  
状态管理：`src/composables/usePlayback.js`

#### EXT-03 报文结构解析

**入口**：TCP / 路由转发 / 综合场景 右侧面板「报文结构解析」

- 按 **Ethernet → IPv4 → TCP/UDP/HTTP** 分层展示字段名、示例值、中文说明
- 当前步相对上一步**变化字段**高亮（如 seq/ack、TTL 递减）
- 字段数据来自步骤配置或 `buildPacketLayers(step)` 纯函数，不解析真实 pcap

共享组件：`src/components/PacketStructurePanel.vue`  
构建函数：`src/utils/buildPacketLayers.js`

#### EXT-04 知识检索问答

**入口**：`/knowledge/search`，或知识体系顶栏搜索框

- 自然语言式问句输入，底层为 keyword 模糊匹配（非大模型）
- 后端 `GET /api/knowledge/search?keyword=&page=&pageSize=` 五表联合检索 `title`、`content`、`tags`
- 结果按层分组，含摘要（前 200 字）、标签、跳转对应层
- 关键词高亮、推荐问题 chips、分页（默认 10 条/页，最大 20）

---

## 7. 页面截图

> 答辩/验收时请补充实际运行截图。建议将图片放入 `docs/screenshots/` 并在下方引用。

| 页面 | 截图 | 说明 |
|------|------|------|
| 首页 | `docs/screenshots/home.png` | 模块导航入口 |
| TCP 三次握手 | `docs/screenshots/tcp-handshake.png` | 拓扑动画 + 报文结构解析 |
| 路由转发 | `docs/screenshots/routing.png` | 路由表高亮与转发步骤 |
| 综合场景 | `docs/screenshots/scenario.png` | 正常模式全流程 |
| 故障模拟 | `docs/screenshots/scenario-fault.png` | DNS 失败 / ARP 错误 / TCP 超时 任一 |
| 知识图谱 | `docs/screenshots/knowledge-graph.png` | 五层树形图 |
| 知识检索 | `docs/screenshots/knowledge-search.png` | 跨层检索结果 |

Markdown 引用示例：

```markdown
![TCP 三次握手](docs/screenshots/tcp-handshake.png)
```

---

## 附录

### 项目结构

```
ClassDemo/
├── src/                          # 前端源码
│   ├── views/                    # 页面
│   ├── components/               # 可视化与共享组件（PlaybackControls、PacketStructurePanel）
│   ├── composables/              # usePlayback 等
│   ├── data/                     # scenarioSteps、faultProfiles
│   ├── utils/                    # buildPacketLayers
│   ├── api/                      # 后端 API 封装
│   └── router/
├── backend/                      # Spring Boot 后端
├── docs/                         # PRD、截图等
├── package.json
└── vite.config.js
```

### 后端 API 概览

所有接口前缀 `/api`，成功时 `code === 0`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查 |
| GET | `/layers` | 五层元数据列表 |
| GET | `/layers/{layerKey}` | 单层详情 |
| PUT | `/layers/{layerKey}` | 更新层元数据 |
| GET | `/layers/{layerKey}/knowledge` | 知识点分页（支持 `keyword`） |
| GET | `/layers/{layerKey}/knowledge/{id}` | 知识点详情 |
| POST/PUT/DELETE | `/layers/{layerKey}/knowledge[/{id}]` | 增删改 |
| GET | `/knowledge-graph` | 知识图谱聚合 |
| GET | `/knowledge/search` | **跨五层联合检索（EXT-04）** |

`layerKey`：`application` | `transport` | `network` | `datalink` | `physical`

### 相关文档

| 文档 | 说明 |
|------|------|
| [docs/PRD.md](docs/PRD.md) | 产品需求、页面规格、接口约定 |
| [docs/module3-todo.md](docs/module3-todo.md) | 模块三开发清单 |

### 许可证

本项目为课程实习作业用途，仅供学习与验收演示。
