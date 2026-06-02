-- 五层元数据（protocols 存 JSON 数组字符串）
-- INSERT IGNORE：首次启动写入；重启不覆盖已有数据
INSERT IGNORE INTO layer_info (id, layer_key, layer_name, main_function, protocols, devices_units) VALUES
(1, 'application', '应用层',
 '为应用程序提供网络服务接口，定义应用进程间通信规则，将用户数据转换为可在网络上传输的格式。',
 '["HTTP","HTTPS","DNS","FTP","SMTP","POP3","IMAP"]',
 '报文 Message、应用进程'),
(2, 'transport', '传输层',
 '提供端到端的可靠或不可靠数据传输服务，负责分段、复用、流量控制与拥塞控制。',
 '["TCP","UDP","SCTP"]',
 '传输层报文段 Segment、端口号'),
(3, 'network', '网络层',
 '负责主机到主机的分组转发与路由选择，实现逻辑寻址与跨网络互联。',
 '["IP","ICMP","IGMP","ARP","OSPF","BGP"]',
 '分组 Packet / 数据报 Datagram、路由器'),
(4, 'datalink', '数据链路层',
 '在相邻节点间可靠地传输帧，进行差错检测、流量控制与介质访问控制。',
 '["Ethernet","PPP","HDLC","802.11 Wi-Fi"]',
 '帧 Frame、交换机、网桥、MAC 地址'),
(5, 'physical', '物理层',
 '在物理介质上传输比特流，定义电气特性、机械接口与传输速率等物理规范。',
 '["RJ-45","光纤","无线电波","双绞线"]',
 '比特 Bit、集线器、中继器、信道带宽');

-- 应用层知识点（≥5 条）
INSERT IGNORE INTO knowledge_application (id, title, content, tags, sort_order) VALUES
(1, 'HTTP 请求响应模型', 'HTTP 基于客户端-服务器模型，无状态。常见方法：GET 获取资源、POST 提交数据、PUT 更新、DELETE 删除。状态码 2xx 成功、3xx 重定向、4xx 客户端错误、5xx 服务器错误。', 'HTTP,Web', 10),
(2, 'DNS 域名解析', 'DNS 将域名解析为 IP 地址，采用分布式层次数据库。解析过程：本地缓存 → 递归查询根/顶级/权威域名服务器，返回 A/AAAA 等记录。', 'DNS,应用层', 20),
(3, 'SMTP 与邮件传输', 'SMTP 用于发送邮件，默认端口 25。邮件从 MUA 经 MTA 中继到收件方 MTA，配合 POP3/IMAP 由客户端拉取或同步邮箱。', 'SMTP,邮件', 30),
(4, 'FTP 文件传输', 'FTP 使用控制连接（21）与数据连接（20 主动 / 随机被动）双通道传输文件，支持 ASCII 与二进制模式。', 'FTP,文件', 40),
(5, 'HTTPS 与 TLS', 'HTTPS = HTTP + TLS，在应用层之下加密传输，防止窃听与篡改。握手阶段协商密码套件、交换证书与密钥。', 'HTTPS,安全', 50),
(6, 'RESTful API 设计', 'REST 以资源为中心，用 URI 标识资源，用 HTTP 动词表达操作，JSON 作为常见数据交换格式，便于前后端分离。', 'HTTP,API', 60);

-- 传输层知识点
INSERT IGNORE INTO knowledge_transport (id, title, content, tags, sort_order) VALUES
(1, 'TCP 三次握手', '建立连接：① 客户端发 SYN；② 服务器 SYN+ACK；③ 客户端 ACK。双方进入 ESTABLISHED，同步初始序列号 ISN。', 'TCP,连接', 10),
(2, 'TCP 四次挥手', '释放连接：① FIN；② ACK；③ FIN；④ ACK。主动关闭方经历 FIN_WAIT、TIME_WAIT 等状态，需等待 2MSL 防止旧报文干扰。', 'TCP,连接', 20),
(3, 'TCP 可靠传输机制', '序号/确认号、超时重传、滑动窗口、快速重传与快速恢复共同保证字节流按序、无差错交付。', 'TCP,可靠', 30),
(4, 'UDP 特点与适用场景', 'UDP 无连接、不保证可靠与有序，首部开销小。适用于 DNS 查询、实时音视频、QUIC 底层等低时延场景。', 'UDP', 40),
(5, '端口号与套接字', '传输层用 IP+端口标识通信端点。熟知端口 0~1023（如 80/443/53），套接字 socket = IP + 端口 + 协议。', '端口,套接字', 50),
(6, '拥塞控制算法', 'TCP 拥塞窗口结合慢启动、拥塞避免、快重传、快恢复，根据丢包与 RTT 动态调节发送速率，避免网络崩溃。', 'TCP,拥塞', 60);

-- 网络层知识点
INSERT IGNORE INTO knowledge_network (id, title, content, tags, sort_order) VALUES
(1, 'IPv4 地址与分类', 'IPv4 32 位，点分十进制表示。早期 A/B/C 类划分，现多用 CIDR 无类编址，子网掩码划分网络与主机位。', 'IP,IPv4', 10),
(2, 'IP 分组首部', '关键字段：版本、首部长度、总长度、标识、标志、片偏移、TTL、协议号、首部校验和、源/目的 IP。TTL 每经一跳减 1。', 'IP,分组', 20),
(3, 'ICMP 与 ping', 'ICMP 传递控制与差错报文，如 Echo Request/Reply（ping）、Destination Unreachable、Time Exceeded（traceroute）。', 'ICMP', 30),
(4, '路由选择与最长前缀匹配', '路由器查路由表，按最长前缀匹配转发。缺省路由 0.0.0.0/0 作为兜底，下一跳指向相邻路由器接口。', '路由,转发', 40),
(5, 'NAT 网络地址转换', 'NAT 将私网地址映射为公网地址，缓解 IPv4 耗尽。NAPT 同时转换 IP 与端口，家用路由器常见。', 'NAT,IP', 50),
(6, '子网划分示例', '给定 192.168.1.0/24，借 3 位主机位可得 8 个子网，每子网可用主机数 2^n-2，需规划网关与广播地址。', '子网,CIDR', 60);

-- 数据链路层知识点
INSERT IGNORE INTO knowledge_datalink (id, title, content, tags, sort_order) VALUES
(1, '以太网帧结构', '以太网 II 帧：目的 MAC、源 MAC、类型（0800=IPv4）、数据、FCS。交换机根据 MAC 地址表转发。', 'Ethernet,帧', 10),
(2, 'MAC 地址', 'MAC 48 位全球唯一（或本地管理），用于同一广播域内链路层寻址，与 IP 配合由 ARP 解析映射。', 'MAC', 20),
(3, 'ARP 地址解析', 'ARP 广播询问「谁有 IP x.x.x.x」，目标主机单播回复 MAC，结果缓存于 ARP 表，有生存时间。', 'ARP', 30),
(4, '交换机与 VLAN', '交换机学习源 MAC 建立转发表，隔离冲突域。VLAN 802.1Q 在帧中加标签，逻辑划分广播域。', '交换机,VLAN', 40),
(5, '差错检测 CRC', '数据链路层常用 CRC 循环冗余校验检测比特差错，出错帧丢弃，由上层 TCP 重传或应用处理。', '差错,CRC', 50),
(6, 'CSMA/CD 与冲突', '传统共享以太网用载波侦听多路访问/冲突检测，冲突后退避重传；全双工交换式以太网基本无冲突。', 'CSMA/CD', 60);

-- 物理层知识点
INSERT IGNORE INTO knowledge_physical (id, title, content, tags, sort_order) VALUES
(1, '比特与信道', '物理层传输比特 0/1，信道带宽限制数据率。奈奎斯特定理与香农定理给出无噪声与有噪声信道容量上限。', '比特,带宽', 10),
(2, '双绞线与 RJ-45', '非屏蔽/屏蔽双绞线用于以太网，RJ-45 接头。T568A/B 线序，直连 vs 交叉线用于不同设备互联。', '双绞线', 20),
(3, '光纤传输', '光信号在纤芯全反射传播，抗电磁干扰、距离远、带宽高。单模光纤长距离骨干，多模用于园区。', '光纤', 30),
(4, '调制与编码', '数字数据需编码为模拟信号或光脉冲，如 NRZ、曼彻斯特编码；无线采用调幅调频或 QAM 等调制。', '编码,调制', 40),
(5, '集线器与中继器', '集线器在物理层放大信号，所有端口同一冲突域；中继器延伸线缆距离，不解析帧内容。', '集线器', 50),
(6, '无线物理层', 'Wi-Fi 使用 2.4/5 GHz 频段，OFDM 等多载波技术，需考虑传播损耗、多径衰落与 CSMA/CA 接入。', 'Wi-Fi,无线', 60);
