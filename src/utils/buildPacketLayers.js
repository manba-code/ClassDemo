/**
 * 从步骤配置构建分层报文结构（EXT-03）
 * @typedef {{ name: string, value: string, desc: string, changed?: boolean }} PacketField
 * @typedef {{ layer: string, fields: PacketField[] }} PacketLayer
 */

function ethLayer(srcMac, dstMac, changedFields = []) {
  if (srcMac === '—' && dstMac === '—') return null
  const mark = (f) => changedFields.includes(f)
  return {
    layer: 'Ethernet',
    fields: [
      { name: 'Dst MAC', value: dstMac, desc: '目的 MAC 地址', changed: mark('Dst MAC') },
      { name: 'Src MAC', value: srcMac, desc: '源 MAC 地址', changed: mark('Src MAC') },
      { name: 'Type', value: '0x0800', desc: '上层协议类型（IPv4）', changed: mark('Type') },
    ],
  }
}

function ipv4Layer(srcIp, dstIp, changedFields = [], ttl = '64') {
  if (srcIp === '—' && dstIp === '—') return null
  const cleanSrc = String(srcIp).split(':')[0]
  const cleanDst = String(dstIp).split(':')[0]
  const mark = (f) => changedFields.includes(f)
  return {
    layer: 'IPv4',
    fields: [
      { name: 'Src IP', value: cleanSrc, desc: '源 IP 地址', changed: mark('Src IP') },
      { name: 'Dst IP', value: cleanDst, desc: '目的 IP 地址', changed: mark('Dst IP') },
      { name: 'TTL', value: ttl, desc: '生存时间，每经一跳减 1', changed: mark('TTL') },
      { name: 'Protocol', value: '—', desc: '上层协议号', changed: mark('Protocol') },
    ],
  }
}

function tcpLayer({ srcPort, dstPort, flags, seq, ack, window = '65535' }, changedFields = []) {
  const mark = (f) => changedFields.includes(f)
  return {
    layer: 'TCP',
    fields: [
      { name: 'Src Port', value: srcPort, desc: '源端口', changed: mark('Src Port') },
      { name: 'Dst Port', value: dstPort, desc: '目的端口', changed: mark('Dst Port') },
      { name: 'Flags', value: flags.join(', ') || '—', desc: '控制位（SYN/ACK/FIN 等）', changed: mark('Flags') },
      { name: 'Seq', value: seq ?? '—', desc: '序列号', changed: mark('Seq') },
      { name: 'Ack', value: ack ?? '—', desc: '确认号', changed: mark('Ack') },
      { name: 'Window', value: window, desc: '接收窗口大小', changed: mark('Window') },
    ],
  }
}

function udpLayer({ srcPort, dstPort, length = '—' }, changedFields = []) {
  const mark = (f) => changedFields.includes(f)
  return {
    layer: 'UDP',
    fields: [
      { name: 'Src Port', value: srcPort, desc: '源端口', changed: mark('Src Port') },
      { name: 'Dst Port', value: dstPort, desc: '目的端口', changed: mark('Dst Port') },
      { name: 'Length', value: length, desc: 'UDP 首部长 + 数据长度', changed: mark('Length') },
    ],
  }
}

function httpLayer({ method = 'GET', path = '/', host = '—' }, changedFields = []) {
  const mark = (f) => changedFields.includes(f)
  return {
    layer: 'HTTP',
    fields: [
      { name: 'Request Line', value: `${method} ${path} HTTP/1.1`, desc: '请求行', changed: mark('Request Line') },
      { name: 'Host', value: host, desc: '请求的主机名', changed: mark('Host') },
    ],
  }
}

function parsePorts(ipStr) {
  const parts = String(ipStr).split(':')
  return parts.length > 1 ? parts[parts.length - 1] : '—'
}

function parseIpOnly(ipStr) {
  return String(ipStr).split(':')[0]
}

/** 综合场景步骤 → 报文层 */
export function buildScenarioPacketLayers(step, prevStep = null) {
  if (!step || step.protocol === '—') return []

  const changed = []
  if (prevStep) {
    if (step.srcMac !== prevStep.srcMac) changed.push('Src MAC', 'Dst MAC')
    if (step.srcIp !== prevStep.srcIp) changed.push('Src IP')
    if (step.dstIp !== prevStep.dstIp) changed.push('Dst IP')
    if (step.protocol !== prevStep.protocol) changed.push('Flags', 'Protocol')
  }

  const layers = []
  const eth = ethLayer(step.srcMac, step.dstMac, changed)
  if (eth) layers.push(eth)

  const proto = step.protocol.toUpperCase()
  const hasIp = step.srcIp !== '—' || step.dstIp !== '—'

  if (hasIp) {
    const ip = ipv4Layer(step.srcIp, step.dstIp, changed)
    if (ip) {
      if (proto.includes('TCP')) ip.fields.find((f) => f.name === 'Protocol').value = '6 (TCP)'
      else if (proto.includes('UDP') || proto.includes('DNS')) ip.fields.find((f) => f.name === 'Protocol').value = '17 (UDP)'
      else if (proto.includes('ARP')) ip.fields.find((f) => f.name === 'Protocol').value = '— (ARP 独立封装)'
      layers.push(ip)
    }
  }

  if (proto.includes('ARP')) {
    layers.push({
      layer: 'ARP',
      fields: [
        { name: 'Opcode', value: step.packetLabel?.includes('Rep') ? 'Reply (2)' : 'Request (1)', desc: 'ARP 操作类型' },
        { name: 'Sender IP', value: parseIpOnly(step.srcIp), desc: '发送方 IP' },
        { name: 'Target IP', value: parseIpOnly(step.dstIp), desc: '目标 IP' },
        { name: 'Sender MAC', value: step.srcMac, desc: '发送方 MAC' },
      ],
    })
    return layers
  }

  if (proto.includes('DNS') || proto.includes('UDP')) {
    layers.push(
      udpLayer(
        { srcPort: parsePorts(step.srcIp), dstPort: parsePorts(step.dstIp) },
        changed
      )
    )
    if (proto.includes('DNS')) {
      layers.push({
        layer: 'DNS',
        fields: [
          {
            name: 'Query/Response',
            value: step.packetLabel?.includes('Reply') ? 'Response (A 记录)' : 'Query (A 记录)',
            desc: 'DNS 报文类型',
          },
          { name: 'Name', value: 'www.abc.com', desc: '查询的域名' },
          { name: 'Type', value: 'A', desc: '记录类型' },
        ],
      })
    }
  }

  if (proto.includes('TCP')) {
    const flags = []
    const label = step.packetLabel ?? ''
    if (label.includes('SYN')) flags.push('SYN')
    if (label.includes('ACK')) flags.push('ACK')
    layers.push(
      tcpLayer(
        {
          srcPort: parsePorts(step.srcIp),
          dstPort: parsePorts(step.dstIp),
          flags,
          seq: label.includes('SYN') && !label.includes('+') ? '1000' : label.includes('ACK') ? '1001' : '—',
          ack: label.includes('ACK') ? '5001' : '—',
        },
        changed
      )
    )
  }

  if (proto.includes('HTTP')) {
    layers.push(httpLayer({ method: 'GET', path: '/', host: 'www.abc.com' }, changed))
  }

  return layers
}

/** TCP 演示步骤详情 → 报文层 */
export function buildTcpPacketLayers(detail, prevDetail = null) {
  if (!detail || detail.direction === '—') return []

  const changed = []
  if (prevDetail) {
    if (detail.seq !== prevDetail.seq) changed.push('Seq')
    if (detail.ack !== prevDetail.ack) changed.push('Ack')
    if (JSON.stringify(detail.flags) !== JSON.stringify(prevDetail.flags)) changed.push('Flags')
  }

  const layers = []
  const eth = ethLayer(detail.srcMac, detail.dstMac, changed)
  if (eth) layers.push(eth)

  const ip = ipv4Layer(detail.srcIp, detail.dstIp, changed)
  if (ip) {
    ip.fields.find((f) => f.name === 'Protocol').value = '6 (TCP)'
    layers.push(ip)
  }

  const srcPort = String(detail.srcIp).split(':')[1] ?? '—'
  const dstPort = String(detail.dstIp).split(':')[1] ?? '—'
  layers.push(
    tcpLayer(
      {
        srcPort,
        dstPort,
        flags: detail.flags ?? [],
        seq: detail.seq != null ? String(detail.seq) : '—',
        ack: detail.ack != null ? String(detail.ack) : '—',
      },
      changed
    )
  )

  return layers
}

/** 路由转发步骤 → 报文层（Ethernet + IPv4） */
export function buildRoutingPacketLayers(step, prevStep = null) {
  if (!step || step.stepNum < 1) return []

  const srcMac = step.stepNum <= 2 ? '00:1A:2B:3C:4D:01' : '00:1A:2B:3C:4D:FE'
  const dstMac = step.stepNum <= 1 ? '00:1A:2B:3C:4D:FE' : step.stepNum <= 3 ? '00:1A:2B:3C:4D:02' : '00:1A:2B:3C:4D:03'

  const changed = []
  if (prevStep && prevStep.stepNum !== step.stepNum) {
    changed.push('Dst MAC', 'Src MAC', 'TTL')
  }

  const ttl = String(Math.max(1, 64 - (step.stepNum - 1) * 2))

  const layers = []
  layers.push(ethLayer(srcMac, dstMac, changed))
  const ip = ipv4Layer('192.168.1.10', '172.16.0.20', changed, ttl)
  ip.fields.find((f) => f.name === 'Protocol').value = '—'
  layers.push(ip)
  layers.push({
    layer: 'Payload',
    fields: [{ name: '载荷', value: '上层协议数据', desc: '无传输层负载时标注为上层数据' }],
  })

  return layers
}
