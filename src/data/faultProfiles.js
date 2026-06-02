/**
 * 网络故障模拟配置（EXT-01）
 * branchFromStep: 从该 stepNum 起替换为故障分支步骤（含结论步）
 */
import { SCENARIO_STEPS, SCENARIO_CONCLUSION } from './scenarioSteps.js'

export const RUN_MODES = [
  { value: 'normal', label: '正常' },
  { value: 'dns_fail', label: 'DNS 失败' },
  { value: 'arp_error', label: 'ARP 错误' },
  { value: 'tcp_timeout', label: 'TCP 超时' },
]

const baseArpDone = {
  arpTable: [{ ip: '192.168.1.1', mac: 'AA:AA:AA:AA:AA:FE' }],
  switchTable: [
    { mac: 'AA:AA:AA:AA:AA:01', port: 'P1 (H1)', note: '已学习' },
    { mac: 'AA:AA:AA:AA:AA:FE', port: 'P4 (R 上联)', note: '学习' },
  ],
}

const baseDnsQueryDone = {
  arpTable: [{ ip: '192.168.1.1', mac: 'AA:AA:AA:AA:AA:FE' }],
  switchTable: [
    { mac: 'AA:AA:AA:AA:AA:01', port: 'P1 (H1)', note: '已学习' },
    { mac: 'AA:AA:AA:AA:AA:FE', port: 'P4 (R 上联)', note: '已学习' },
  ],
}

const baseDnsReplyDone = {
  arpTable: [
    { ip: '192.168.1.1', mac: 'AA:AA:AA:AA:AA:FE' },
    { ip: '192.168.1.53', mac: 'AA:AA:AA:AA:AA:53' },
  ],
  switchTable: [
    { mac: 'AA:AA:AA:AA:AA:01', port: 'P1 (H1)', note: '已学习' },
    { mac: 'AA:AA:AA:AA:AA:53', port: 'P3 (DNS)', note: '学习' },
    { mac: 'AA:AA:AA:AA:AA:FE', port: 'P4 (R 上联)', note: '已学习' },
  ],
}

/** @type {Record<string, import('./scenarioSteps.js').ScenarioFaultProfile>} */
export const FAULT_PROFILES = {
  dns_fail: {
    mode: 'dns_fail',
    label: 'DNS 解析失败',
    branchFromStep: 4,
    faultCode: 'NXDOMAIN',
    conclusion: 'H1 无法获得 www.abc.com 的 IP，访问终止。',
    normalCompare: '正常模式下本地 DNS 应返回 A 记录 203.0.113.80，随后 H1 发起 TCP 连接。',
    steps: [
      {
        stepNum: 4,
        name: 'H1 向本地 DNS 查询',
        from: 'H1',
        to: '本地 DNS',
        protocol: 'DNS / UDP',
        purpose: '查询 www.abc.com 的 A 记录',
        transmission: '单播',
        srcMac: 'AA:AA:AA:AA:AA:01',
        dstMac: 'AA:AA:AA:AA:AA:53',
        srcIp: '192.168.1.10:随机端口',
        dstIp: '192.168.1.53:53',
        pathNodes: ['h1', 's', 'dns'],
        pathLinks: ['h1-s', 'dns-s'],
        animate: true,
        packetLabel: 'DNS Query',
        switchBehavior: '目的 MAC 未知 → 泛洪',
        switchAction: 'flood',
        ...baseDnsQueryDone,
        summary: 'H1 发送 DNS 查询报文',
      },
      {
        stepNum: 5,
        name: 'DNS 返回失败（NXDOMAIN）',
        from: '本地 DNS',
        to: 'H1',
        protocol: 'DNS / UDP',
        purpose: '域名无 A 记录或解析失败',
        transmission: '单播',
        srcMac: 'AA:AA:AA:AA:AA:53',
        dstMac: 'AA:AA:AA:AA:AA:01',
        srcIp: '192.168.1.53:53',
        dstIp: '192.168.1.10:随机端口',
        pathNodes: ['dns', 's', 'h1'],
        pathLinks: ['dns-s', 'h1-s'],
        animate: true,
        animateReverse: true,
        packetLabel: 'DNS Fail',
        isFault: true,
        faultCode: 'NXDOMAIN',
        faultReason: '域名无法解析，浏览器无法获得目的 IP',
        switchBehavior: 'DNS 返回 RCODE=3（Name Error）',
        switchAction: 'forward',
        ...baseDnsQueryDone,
        summary: 'DNS 返回 NXDOMAIN，无 A 记录',
      },
      {
        stepNum: 6,
        name: '访问失败结论',
        from: 'H1',
        to: '—',
        protocol: '—',
        purpose: '流程终止：无法继续 TCP/HTTP',
        transmission: '—',
        srcMac: '—',
        dstMac: '—',
        srcIp: '—',
        dstIp: '—',
        pathNodes: ['h1'],
        pathLinks: [],
        animate: false,
        isFaultConclusion: true,
        faultCode: 'NXDOMAIN',
        switchBehavior: '无后续通信',
        switchAction: 'none',
        ...baseDnsQueryDone,
        summary: 'H1 无法获得 www.abc.com 的 IP，访问终止',
      },
    ],
  },

  arp_error: {
    mode: 'arp_error',
    label: 'ARP 错误',
    branchFromStep: 3,
    faultCode: 'ARP_TIMEOUT',
    conclusion: 'H1 无法获得网关 MAC，二层无法送达，三层报文无法发出。',
    normalCompare: '正常模式下路由器 R 应单播 ARP Reply，H1 ARP 表写入 192.168.1.1 → FE。',
    steps: [
      {
        stepNum: 3,
        name: 'ARP 超时（无 Reply）',
        from: '—',
        to: '—',
        protocol: 'ARP',
        purpose: '网关未响应或 MAC 错误，H1 ARP 表不更新',
        transmission: '—',
        srcMac: '—',
        dstMac: '—',
        srcIp: '192.168.1.1',
        dstIp: '—',
        pathNodes: ['h1', 's'],
        pathLinks: ['h1-s'],
        animate: true,
        animateReverse: false,
        packetLabel: 'ARP Timeout',
        isFault: true,
        isFaultPath: true,
        faultCode: 'ARP_TIMEOUT',
        faultReason: '二层无法送达网关，三层报文无法发出',
        switchBehavior: '无 ARP Reply；H1 等待超时',
        switchAction: 'none',
        arpTable: [],
        switchTable: [{ mac: 'AA:AA:AA:AA:AA:01', port: 'P1 (H1)', note: '已学习' }],
        summary: 'ARP 请求超时，H1 ARP 表仍为空',
      },
      {
        stepNum: 4,
        name: '二层转发失败结论',
        from: 'H1',
        to: '—',
        protocol: '—',
        purpose: '无法封装以太网帧发往网关',
        transmission: '—',
        srcMac: '—',
        dstMac: '—',
        srcIp: '—',
        dstIp: '—',
        pathNodes: ['h1'],
        pathLinks: [],
        animate: false,
        isFaultConclusion: true,
        faultCode: 'ARP_TIMEOUT',
        switchBehavior: '流程终止',
        switchAction: 'none',
        arpTable: [],
        switchTable: [{ mac: 'AA:AA:AA:AA:AA:01', port: 'P1 (H1)', note: '已学习' }],
        summary: 'H1 无法获得网关 MAC，后续 DNS/TCP 均无法执行',
      },
    ],
  },

  tcp_timeout: {
    mode: 'tcp_timeout',
    label: 'TCP 超时',
    branchFromStep: 6,
    faultCode: 'TCP_TIMEOUT',
    conclusion: 'TCP 连接建立失败，可能原因：路由、防火墙、服务未监听。',
    normalCompare: '正常模式下 Web Server 应回复 SYN+ACK，完成三次握手后继续 HTTP。',
    steps: [
      {
        stepNum: 7,
        name: 'SYN 重传 #1（Server 无响应）',
        from: 'H1',
        to: 'Web Server',
        protocol: 'TCP',
        purpose: '超时后第 1 次重传',
        transmission: '单播',
        srcMac: 'AA:AA:AA:AA:AA:01',
        dstMac: 'AA:AA:AA:AA:AA:FE',
        srcIp: '192.168.1.10:随机',
        dstIp: '203.0.113.80:443',
        pathNodes: ['h1', 's', 'r', 'internet', 'web'],
        pathLinks: ['h1-s', 's-r', 'r-inet', 'inet-web'],
        animate: true,
        packetLabel: 'SYN (retry)',
        isFault: true,
        isFaultPath: true,
        faultCode: 'TCP_TIMEOUT',
        switchBehavior: 'Server 未监听或路径不可达',
        switchAction: 'forward',
        ...baseDnsReplyDone,
        summary: 'SYN 重传 #1，仍无 SYN+ACK',
      },
      {
        stepNum: 8,
        name: 'SYN 重传 #2 后超时失败',
        from: 'H1',
        to: '—',
        protocol: 'TCP',
        purpose: '连接建立失败',
        transmission: '—',
        srcMac: '—',
        dstMac: '—',
        srcIp: '192.168.1.10',
        dstIp: '203.0.113.80',
        pathNodes: ['h1'],
        pathLinks: [],
        animate: false,
        isFault: true,
        isFaultConclusion: true,
        faultCode: 'TCP_TIMEOUT',
        faultReason: '连接建立失败，可能原因：路由、防火墙、服务未监听',
        switchBehavior: 'H1 放弃连接',
        switchAction: 'none',
        ...baseDnsReplyDone,
        summary: 'TCP 连接超时失败，流程终止',
      },
    ],
  },
}

/**
 * 根据运行模式合并步骤列表
 * @param {'normal'|'dns_fail'|'arp_error'|'tcp_timeout'} runMode
 */
export function getStepsForMode(runMode) {
  if (runMode === 'normal') return SCENARIO_STEPS

  const profile = FAULT_PROFILES[runMode]
  if (!profile) return SCENARIO_STEPS

  const prefix = SCENARIO_STEPS.filter((s) => s.stepNum < profile.branchFromStep)
  return [...prefix, ...profile.steps]
}

export function getConclusionForMode(runMode) {
  if (runMode === 'normal') return SCENARIO_CONCLUSION

  const profile = FAULT_PROFILES[runMode]
  if (!profile) return SCENARIO_CONCLUSION

  return {
    title: `故障演示完成 · ${profile.label}`,
    points: [
      profile.conclusion,
      `故障码：${profile.faultCode}`,
      profile.normalCompare,
    ],
    isFault: true,
    faultCode: profile.faultCode,
  }
}

export function getFaultProfile(runMode) {
  return runMode === 'normal' ? null : FAULT_PROFILES[runMode] ?? null
}
