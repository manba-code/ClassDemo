-- 修复中文乱码：清空种子数据后重启 Spring Boot，将按 UTF-8 重新执行 seed.sql
-- 用法（Docker MySQL）：
--   Get-Content backend/scripts/fix-encoding-reseed.sql | docker exec -i classdemo-mysql mysql -uclassdemo -pclassdemo123 --default-character-set=utf8mb4 classdemo_network
-- 然后重启 backend（mvn spring-boot:run）

SET NAMES utf8mb4;

TRUNCATE TABLE knowledge_application;
TRUNCATE TABLE knowledge_transport;
TRUNCATE TABLE knowledge_network;
TRUNCATE TABLE knowledge_datalink;
TRUNCATE TABLE knowledge_physical;
TRUNCATE TABLE layer_info;
