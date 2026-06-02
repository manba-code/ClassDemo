# 计算机网络知识体系交互式展示系统

面向《网络实习》课程的 **Web 交互式教学系统**，集协议过程可视化、综合通信场景模拟、TCP/IP 五层知识体系管理与知识图谱展示于一体。

| 项 | 说明 |
|----|------|
| 适用课程 | 《网络实习》 |
| 前端 | Vue 3 + Vite + Vue Router + Tailwind CSS |
| 后端 | Java 17 + Spring Boot 3 + MyBatis-Plus |
| 数据库 | MySQL 8.x |
| 详细需求 | 见 [docs/PRD.md](docs/PRD.md) |

---

## 功能模块

| 模块 | 路由 | 说明 | 状态 |
|------|------|------|------|
| 首页导航 | `/` | 系统介绍与各模块入口卡片 | ✅ |
| 题目 2 · TCP | `/protocol/tcp` | 三次握手 / 四次挥手，报文动画与状态机 | ✅ |
| 题目 5 · 路由转发 | `/protocol/routing` | 跨网段转发、路由表高亮与分步演示 | ✅ |
| 综合场景模拟 | `/scenario` | H1 访问 `www.abc.com`，ARP/DNS/TCP/HTTP 分步演示 | ✅ |
| 知识体系 | `/knowledge/*` | 五层 Tab 知识点 CRUD、分页检索 | ✅（需后端） |
| 知识图谱 | `/knowledge/graph` | 五层 + 知识点树形结构可视化（ECharts） | ✅（需后端） |

协议类页面统一采用 **左栏拓扑与控制 + 右栏步骤详情/日志** 布局，支持「开始 / 下一步 / 重置」分步交互与报文传递动画。

---

## 技术栈

### 前端

- Vue 3、Vite 6、Vue Router 4
- Tailwind CSS 3、lucide-vue-next 图标
- ECharts 6（知识图谱）
- 开发端口默认 `5173`，`/api` 代理至后端 `8080`

### 后端

- Spring Boot 3.2、MyBatis-Plus 3.5
- REST API 统一前缀 `/api`
- 统一响应体 `ApiResponse<T>`（`code` / `message` / `data`）
- 本地演示模式，无登录鉴权

### 数据库

- MySQL 8.x，库名 `classdemo_network`
- 启动时自动执行 `schema.sql`、`seed.sql`（见 `application.yml` 中 `spring.sql.init`）

---

## 项目结构

```
ClassDemo/
├── src/                          # 前端源码
│   ├── views/                    # 页面（首页、协议、场景、知识体系）
│   ├── components/               # 可视化与通用组件
│   ├── layouts/                  # 布局（AppLayout、KnowledgeLayout）
│   ├── api/                      # 后端 API 封装
│   └── router/                   # 路由配置
├── backend/                      # Spring Boot 后端
│   ├── src/main/java/.../network/
│   │   ├── controller/           # REST 控制器
│   │   ├── service/              # 业务逻辑
│   │   ├── mapper/               # MyBatis-Plus Mapper
│   │   └── entity/ dto/ common/  # 实体、DTO、公共类
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/                   # schema.sql、seed.sql
│   ├── docker-compose.yml        # 本地 MySQL 容器
│   └── scripts/                  # init-mysql.sql 等
├── docs/
│   ├── PRD.md                    # 产品需求文档
│   └── module3-todo.md           # 模块三开发清单
├── package.json
└── vite.config.js
```

---

## 快速开始

### 环境要求

| 组件 | 版本 |
|------|------|
| Node.js | 18+（推荐 LTS） |
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.x（或 Docker） |

### 1. 启动 MySQL

**方式一：Docker（推荐）**

在项目根目录执行：

```bash
docker compose -f backend/docker-compose.yml up -d
```

默认创建库 `classdemo_network`，用户 `classdemo` / 密码 `classdemo123`，端口 `3306`。

**方式二：本机 MySQL**

```bash
mysql -u root -p < backend/scripts/init-mysql.sql
```

账号需与 `backend/src/main/resources/application.yml` 中 `spring.datasource` 一致。

更多说明见 [backend/README.md](backend/README.md)。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

服务地址：`http://localhost:8080/api`  
健康检查：`GET http://localhost:8080/api/health`

首次启动会自动建表并导入种子数据。可用以下命令验证：

```bash
mysql -u classdemo -pclassdemo123 classdemo_network -e "SHOW TABLES; SELECT COUNT(*) FROM layer_info;"
```

应看到 6 张表，`layer_info` 5 行，各 `knowledge_*` 表至少 5 行。

### 3. 启动前端

在项目根目录：

```bash
npm install
npm run dev
```

浏览器访问：`http://localhost:5173`

生产构建：

```bash
npm run build
npm run preview
```

---

## 页面路由

```
/                           首页
/protocol/tcp               TCP 三次握手 / 四次挥手
/protocol/routing           路由转发
/scenario                   综合网络场景模拟
/knowledge/application      应用层 · 知识体系
/knowledge/transport        传输层
/knowledge/network          网络层
/knowledge/datalink         数据链路层
/knowledge/physical         物理层
/knowledge/graph            知识图谱
```

---

## 后端 API 概览

所有接口前缀为 `/api`，成功时 `code === 0`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/health` | 健康检查 |
| GET | `/layers` | 五层元数据列表 |
| GET | `/layers/{layerKey}` | 单层详情 |
| PUT | `/layers/{layerKey}` | 更新层元数据 |
| GET | `/layers/{layerKey}/knowledge` | 知识点分页列表（支持 `keyword`） |
| GET | `/layers/{layerKey}/knowledge/{id}` | 知识点详情 |
| POST | `/layers/{layerKey}/knowledge` | 新增知识点 |
| PUT | `/layers/{layerKey}/knowledge/{id}` | 更新知识点 |
| DELETE | `/layers/{layerKey}/knowledge/{id}` | 删除知识点 |
| GET | `/knowledge-graph` | 知识图谱聚合数据 |

`layerKey` 取值：`application` | `transport` | `network` | `datalink` | `physical`

---

## 数据库设计

| 表名 | 用途 |
|------|------|
| `layer_info` | TCP/IP 五层元数据（功能、协议、设备等） |
| `knowledge_application` | 应用层知识点 |
| `knowledge_transport` | 传输层知识点 |
| `knowledge_network` | 网络层知识点 |
| `knowledge_datalink` | 数据链路层知识点 |
| `knowledge_physical` | 物理层知识点 |

建表与初始数据脚本：

- `backend/src/main/resources/db/schema.sql`
- `backend/src/main/resources/db/seed.sql`

---

## 配置说明

### 前端代理

`vite.config.js` 将 `/api` 代理到 `http://localhost:8080`，与后端 `server.servlet.context-path: /api` 对应。

### 后端数据源

编辑 `backend/src/main/resources/application.yml` 可修改 MySQL 连接信息。默认：

- URL：`jdbc:mysql://localhost:3306/classdemo_network`
- 用户名：`classdemo`
- 密码：`classdemo123`

若中文显示乱码，可参考 `backend/scripts/fix-encoding-reseed.sql` 修复字符集后重新导入种子数据。

---

## 相关文档

| 文档 | 说明 |
|------|------|
| [docs/PRD.md](docs/PRD.md) | 产品需求、页面规格、接口约定 |
| [docs/module3-todo.md](docs/module3-todo.md) | 模块三（知识体系）开发待办与进度 |
| [backend/README.md](backend/README.md) | 后端环境与 MySQL 启动说明 |
| [docs/《网络实习》实习任务与指导书.pdf](docs/《网络实习》实习任务与指导书.pdf) | 课程原始任务书 |

---

## 验收要点（摘要）

- 至少 2 个协议可视化页面可正常运行（TCP、路由转发）
- 综合场景步骤拆解正确，ARP 表与交换表可动态更新
- 五层模型各子页可用，知识点增删改后刷新不丢失（依赖 MySQL + 后端）
- 具备首页导航，模块间可跳转
- 提供 README、建表脚本；演示时建议附带关键页面截图

---

## 许可证

本项目为课程实习作业用途，仅供学习与验收演示。
