-- 在 MySQL 客户端或 Workbench 中执行，创建本项目使用的库与用户
-- 账号密码需与 application.yml 中 spring.datasource 配置一致

CREATE DATABASE IF NOT EXISTS classdemo_network
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'classdemo'@'localhost' IDENTIFIED BY 'classdemo123';
GRANT ALL PRIVILEGES ON classdemo_network.* TO 'classdemo'@'localhost';
FLUSH PRIVILEGES;
