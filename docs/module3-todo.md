# 模块三开发待办清单

> **第三部分：知识体系与知识图谱**（15 分）  
> 依据：[PRD.md](./PRD.md) v1.3  
> 更新日期：2026-06-02

**目标**：Java Spring Boot + **MySQL 8.x** 持久化 + Vue 五层 Tab CRUD + 知识图谱；路由前缀 `/knowledge/*`，后端 `http://localhost:8080/api`。

**里程碑对齐**（PRD §10）：6/8 完成 API + 五层 Tab；6/9 完成图谱 + 导航打通。

---

## 阶段 A：工程与基础设施

| # | 待办项 | PRD 依据 | 优先级 | 状态 |
|---|--------|----------|--------|------|
| A-00 | 安装 MySQL 8.x；执行 `backend/scripts/init-mysql.sql` 创建库 `classdemo_network` 及用户 | §6.4、§NFR-05 | P0 | 脚本就绪，需本机执行 |
| A-01 | 创建 `backend/` Maven 工程：`pom.xml`（Spring Boot 3、MySQL Connector/J、MyBatis-Plus 或 JPA） | §1.3、§9、§12.1 | P0 | ✅ |
| A-02 | 主类 `NetworkApplication.java`，包结构 `com.classdemo.network.{controller,service,mapper,entity,dto,config,common}` | §4.4.0、§9 | P0 | ✅ |
| A-03 | `application.yml`：端口 8080、MySQL 数据源（url/username/password）、`spring.sql.init` 加载 schema/seed | §6.4、§9 | P0 | ✅ |
| A-04 | 编写 `backend/src/main/resources/db/schema.sql`（MySQL 语法：`layer_info` + 五张 `knowledge_*` 表 + 索引） | §6.2～6.3 | P0 | ✅ |
| A-05 | 编写 `backend/src/main/resources/db/seed.sql`（五层元数据 + 每层 ≥5 条示例知识点） | §6.4 | P0 | ✅ |
| A-06 | 确认 MySQL 已建库且启动后 schema/seed 执行成功，数据可查询 | §6.4、§11.3 | P0 | 待本机验证 |
| A-07 | 前端 `vite.config.js` 配置 `/api` → `8080` 代理 | §8.1 | P0 | ✅ |
| A-08 | 创建 `src/api/client.js`、`layers.js`、`knowledge.js`、`knowledgeGraph.js` | §8.2 | P0 | ✅ |

---

## 阶段 B：后端公共能力（BE-*）

| # | 待办项 | PRD 依据 | 优先级 | 状态 |
|---|--------|----------|--------|------|
| B-01 | **BE-01** REST API 统一前缀 `/api` | §4.4.0 | P0 | ✅ |
| B-02 | **BE-02** `ApiResponse<T>`（`code` / `message` / `data`）及 `ok()` / `fail()` | §7.2、§7.3 | P0 | ✅ |
| B-03 | **BE-03** `GlobalExceptionHandler`：参数校验 → `40001`；资源不存在 → `40401`；无效 `layerKey` → `40402`；其它 → `50000` | §7.3、§4.4.0 | P0 | ✅ |
| B-04 | **BE-04** CORS 放行 `http://localhost:5173` | §7.1、§4.4.0 | P0 | ✅ |
| B-05 | **BE-05** 启动时自动执行 schema + seed（目标 MySQL 库需预先创建） | §4.4.0、§6.4 | P0 | ✅ |
| B-06 | **BE-06** Controller / Service / Mapper 分层；五层知识点分表路由 | §4.4.0、§7.4 | P0 | ✅ |
| B-07 | **BE-07** 不实现登录鉴权（本地演示） | §4.4.0、§NFR-04 | — | ✅ |
| B-08 | 实体/DTO：`LayerInfo`、`KnowledgePoint`；字段驼峰与 DB 下划线映射 | §6.2～6.3、§7.6 | P0 | ✅ |
| B-09 | `GET /api/health` 健康检查 | §7.5、§7.6.1 | P0 | ✅ |

---

## 阶段 C：分层元数据 API

| # | 待办项 | PRD 依据 | 优先级 | 状态 |
|---|--------|----------|--------|------|
| C-01 | `LayerController.listAll()` → `GET /api/layers` | §7.5、§7.6.2 | P0 | ✅ |
| C-02 | `LayerController.getOne()` → `GET /api/layers/{layerKey}` | §7.6.3 | P0 | ✅ |
| C-03 | `LayerController.update()` → `PUT /api/layers/{layerKey}` | §7.6.4 | P1 | ✅ |
| C-04 | `layerKey` 校验：`application\|transport\|network\|datalink\|physical` | §7.4 | P0 | ✅ |
| C-05 | `protocols` 存取：JSON 数组或约定格式，与前端展示一致 | §6.2 | P0 | ✅ |

---

## 阶段 D：知识点 CRUD API

| # | 待办项 | PRD 依据 | 优先级 |
|---|--------|----------|--------|
| D-01 | `GET /api/layers/{layerKey}/knowledge`：分页 + `keyword` 模糊匹配 title/content/tags | §7.6.5、KN-08 | P0 |
| D-02 | `GET /api/layers/{layerKey}/knowledge/{id}` 详情 | §7.6.6 | P0 |
| D-03 | `POST` 新增：必填 `title`、`content`；可选 `tags`、`sortOrder` | §7.6.7、KN-05 | P0 |
| D-04 | `PUT` 更新：字段可选但至少一项 | §7.6.8、KN-06 | P0 |
| D-05 | `DELETE` 删除：响应 `{ id, deleted: true }` | §7.6.9、KN-07 | P0 |
| D-06 | 列表响应含 `pagination`（page、pageSize、total、totalPages） | §7.2 | P0 |
| D-07 | 创建/更新时维护 `created_at`、`updated_at` | §6.3 | P0 |
| D-08 | 五张表分别实现 Mapper/Repository | §6.3、§7.4 | P0 |
| D-09 | 联调验证：**KN-09** 增删改后刷新页面数据不丢失 | §4.4.1 | P0 |

---

## 阶段 E：知识图谱聚合 API（可选但推荐）

| # | 待办项 | PRD 依据 | 优先级 | 状态 |
|---|--------|----------|--------|------|
| E-01 | `GET /api/knowledge-graph`：根「计算机网络」→ 五层 → 知识点 | §7.6.10 | P1 | ✅ |
| E-02 | 节点结构含 `name`、`layerKey`、`type`、`id`、`content` 等 | §7.6.10 | P1 | ✅ |
| E-03 | 若不做该接口，前端五次调用列表 API 聚合（KG-03 方案 B） | §7.6.10、§4.4.2 | P1 | —（已实现 E-01，前端可直接调用聚合接口） |

---

## 阶段 F：前端路由与导航

| # | 待办项 | PRD 依据 | 优先级 |
|---|--------|----------|--------|
| F-01 | 注册 6 条路由：`/knowledge/application\|transport\|network\|datalink\|physical\|graph` | §3 | P0 |
| F-02 | `KnowledgeLayout.vue`：五层 Tab +「知识图谱」Tab，当前路由高亮 | §11.3 | P0 |
| F-03 | `AppLayout.vue` 顶部导航增加「知识体系」入口 | §COM-02、§11.4 | P0 |
| F-04 | `HomeView.vue` 模块卡片：`to: '/knowledge/application'`，状态可点击 | §COM-01 | P0 |
| F-05 | 路由 `meta.title` 与 `document.title` 联动 | 现有 router | P1 |
| F-06 | 默认重定向：`/knowledge` → `/knowledge/application`（可选） | — | P2 |

---

## 阶段 G：五层知识点页面（KN-*）

每个分层页（应用/传输/网络/数据链路/物理）须实现：

| # | 待办项 | PRD 依据 | 优先级 |
|---|--------|----------|--------|
| G-01 | **KN-01** 展示本层「主要功能」 | §4.4.1 | P0 |
| G-02 | **KN-02** 展示「常见协议或技术」 | §4.4.1 | P0 |
| G-03 | **KN-03** 展示「相关设备或数据单位」 | §4.4.1 | P0 |
| G-04 | **KN-04** 知识点列表 + 分页控件 | §4.4.1 | P0 |
| G-05 | **KN-05**「新增」表单/弹窗 → POST | §4.4.1 | P0 |
| G-06 | **KN-06**「编辑」→ PUT | §4.4.1 | P0 |
| G-07 | **KN-07**「删除」确认后 DELETE，列表刷新 | §4.4.1 | P0 |
| G-08 | **KN-08** 搜索框 + keyword 查询 | §4.4.1 | P0 |
| G-09 | 抽取共用组件 `KnowledgeLayerView.vue`，`layerKey` prop 复用五页 | §9 | P1 |
| G-10 | 加载中 / 空列表 / API 错误提示 | §7.2 | P1 |
| G-11 | 表单校验：title、content 非空 | §7.6.7 | P0 |

**知识点字段**（§4.4.1，MySQL）：

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | BIGINT AUTO_INCREMENT | 自动 | 主键 |
| title | VARCHAR(255) | 是 | 知识点标题 |
| content | TEXT | 是 | 详细内容 |
| tags | VARCHAR(512) | 否 | 标签，逗号分隔 |
| sort_order | INT | 否 | 排序权重，默认 0 |
| created_at | DATETIME | 自动 | 创建时间 |
| updated_at | DATETIME | 自动 | 更新时间 |

---

## 阶段 H：知识图谱页（KG-*）

| # | 待办项 | PRD 依据 | 优先级 |
|---|--------|----------|--------|
| H-01 | `KnowledgeGraphView.vue`，路由 `/knowledge/graph` | §4.4.2 | P0 |
| H-02 | **KG-01** 树形或力导向图：根 → 五层 → 知识点 | §4.4.2 | P0 |
| H-03 | **KG-02** 点击节点：展示 title + content | §4.4.2 | P0 |
| H-04 | **KG-03** 数据来自聚合 API 或五层 list 前端组装 | §4.4.2 | P1 |
| H-05 | **KG-04** 图结构不持久化到数据库 | §4.4.2 | — |
| H-06 | 选型图库，保证 Chrome/Edge 流畅 | §NFR-01、§NFR-02 | P0 |

---

## 阶段 I：联调、测试与验收

| # | 待办项 | PRD 依据 | 优先级 |
|---|--------|----------|--------|
| I-01 | MySQL 服务已启动；后端独立启动：`mvn spring-boot:run`，8080 `/api/health` 可访问 | §11.3、§NFR-05 | P0 |
| I-02 | 前端 `npm run dev`，代理联调五层 CRUD 全流程 | §11.3 | P0 |
| I-03 | 五层 + 知识图谱共 6 个 Tab 均可访问 | §11.3 | P0 |
| I-04 | 增删改查 + 模糊查询 + 分页 | §11.3 | P0 |
| I-05 | 刷新后数据仍在 MySQL 中 | §11.3、KN-09 | P0 |
| I-06 | 非法 `layerKey`、不存在 id 返回正确错误码 | §7.3 | P1 |
| I-07 | `pageSize` 上限 50 校验 | §7.6.5 | P2 |

---

## 阶段 J：文档与工程交付

| # | 待办项 | PRD 依据 | 优先级 |
|---|--------|----------|--------|
| J-01 | README：第三部分启动步骤（MySQL 建库、JDK、Maven、双进程、端口） | §NFR-05、§11.4 | P0 |
| J-02 | README：API 列表或指向 PRD 第 7 章 | §11.4 | P0 |
| J-03 | 提交 `schema.sql`、`seed.sql` | §11.4 | P0 |
| J-04 | Git 分阶段 commit（后端 / 路由 / 五层页 / 图谱） | §NFR-06 | P0 |
| J-05 | 演示截图或短视频 | §2.4、§10 | P1 |

---

## 可选加分（非模块三必做）

| # | 待办项 | PRD 依据 |
|---|--------|----------|
| O-01 | **EXT-04** 知识检索问答增强（基于 keyword） | §4.5 |
| O-02 | 分层元数据在线编辑（PUT `/api/layers/{layerKey}`） | §7.6.4 |

---

## PRD 需求编号速查

| 类别 | 编号 | 说明 |
|------|------|------|
| 后端约定 | BE-01～BE-07 | Spring Boot、统一响应、CORS、分表等 |
| 五层页面 | KN-01～KN-09 | 层信息展示 + CRUD + 持久化 |
| 知识图谱 | KG-01～KG-04 | 可视化与节点详情（KG-04 为约束） |
| API 端点 | 10 个 | health、layers×3、knowledge×5、graph×1 |
| 数据表 | 6 张 | `layer_info` + 5×`knowledge_*` |

### 分层枚举 `layerKey`（§7.4）

| layerKey | 中文名 | 知识点表 |
|----------|--------|----------|
| `application` | 应用层 | `knowledge_application` |
| `transport` | 传输层 | `knowledge_transport` |
| `network` | 网络层 | `knowledge_network` |
| `datalink` | 数据链路层 | `knowledge_datalink` |
| `physical` | 物理层 | `knowledge_physical` |

### 信息架构（§3）

```
/knowledge
├── /knowledge/application   应用层
├── /knowledge/transport     传输层
├── /knowledge/network       网络层
├── /knowledge/datalink      数据链路层
├── /knowledge/physical      物理层
└── /knowledge/graph         知识图谱
```

---

## 建议实施顺序

```
A-00 MySQL 环境 → A 工程基建 → B 后端公共 → C 分层 API + D 知识点 CRUD
                ↓
         E 图谱聚合 API（可与 G 并行）
                ↓
A-07/A-08 + F 前端路由 → G 五层页面 → H 知识图谱 → I 验收 → J 文档
```

1. **A-00 + A + B + C + D** — MySQL 就绪后，后端 Postman 自测  
2. **A-07/A-08 + F** — 路由与 API 封装  
3. **G** — 五层 CRUD（先打通一层再复制）  
4. **E + H** — 知识图谱  
5. **I + J** — 验收与 README  

---

## 验收检查清单（PRD §11.3）

- [ ] 五层 + 知识图谱共 6 个 Tab
- [ ] Java Spring Boot 后端可独立启动（8080）
- [ ] 知识点存 MySQL 数据库
- [ ] 增删改查 + 模糊查询 + 分页
- [ ] 刷新后数据不丢失
- [ ] 前端 Vite 代理 `/api` → 8080 联调通过
- [ ] `backend/src/main/resources/db/schema.sql` / `seed.sql` 已提交
- [ ] README 第三部分说明完整

---

## 实现现状（2026-06-02 阶段 C 完成后）

| 项 | 状态 |
|----|------|
| A-00 MySQL 建库脚本 | ✅ `init-mysql.sql` + `setup-mysql.ps1` + `docker-compose.yml` |
| A-01～A-05 后端基建 | ✅ `pom.xml`（Java 17）、`NetworkApplication`、`application.yml`、`schema.sql`、`seed.sql` |
| A-06 库表与种子数据 | ⏳ 需本机执行 A-00 后 `mvn spring-boot:run` 验证 |
| A-07 Vite `/api` 代理 | ✅ `vite.config.js` |
| A-08 `src/api/*` | ✅ `client.js`、`layers.js`、`knowledge.js`、`knowledgeGraph.js` |
| B-01～B-09 后端公共能力 | ✅ `ApiResponse`、`GlobalExceptionHandler`、CORS、`context-path: /api`、分表路由、`GET /api/health` |
| C-01～C-05 分层元数据 API | ✅ `LayerController`、`LayerInfoDto`、`LayerUpdateRequest`、`LayerInfoConverter` |
| E-01～E-02 知识图谱聚合 API | ✅ `KnowledgeGraphController`、`KnowledgeGraphService`、图谱 DTO |
| `/knowledge/*` 路由 | 未注册（阶段 F） |
| 首页知识体系卡片 | `to: null`，待开发（阶段 F） |

完成某项后，可将上表对应行改为「已完成」，或在各阶段表格中将 `#` 列勾选记录进度。
