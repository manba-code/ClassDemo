# 模块三后端（阶段 A 基建）

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x（本地安装或 Docker）

## 方式一：Docker 启动 MySQL（推荐）

在项目根目录执行：

```bash
docker compose -f backend/docker-compose.yml up -d
```

等待健康检查通过后，数据库 `classdemo_network` 与用户 `classdemo` / `classdemo123` 已就绪。

## 方式二：本机 MySQL

在 MySQL 客户端执行：

```bash
mysql -u root -p < backend/scripts/init-mysql.sql
```

账号需与 `src/main/resources/application.yml` 中 `spring.datasource` 一致。

## 启动后端

```bash
cd backend
mvn spring-boot:run
```

首次启动会自动执行 `db/schema.sql` 与 `db/seed.sql`（`spring.sql.init.mode: always`）。

## 验证数据（A-06）

```bash
mysql -u classdemo -pclassdemo123 classdemo_network -e "SHOW TABLES; SELECT COUNT(*) FROM layer_info; SELECT COUNT(*) FROM knowledge_application;"
```

应看到 6 张表，`layer_info` 5 行，各 `knowledge_*` 表至少 5 行。
