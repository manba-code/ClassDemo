<template>
  <div
    class="min-h-screen bg-slate-50 text-slate-800 font-sans selection:bg-teal-200/60"
  >
    <!-- Header -->
    <header class="border-b border-slate-200 bg-white/90 backdrop-blur-sm px-6 py-4 shadow-sm">
      <div class="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="text-xl font-semibold tracking-tight text-slate-900">
            TCP 三次握手与四次挥手
          </h1>
          <p class="text-sm text-slate-500 mt-0.5">交互式协议可视化 · RFC 793 状态机</p>
        </div>
        <div
          class="flex items-center gap-2 text-xs font-mono px-3 py-1.5 rounded-full border border-slate-200 bg-white text-slate-600 shadow-sm"
        >
          <span
            class="w-2 h-2 rounded-full transition-colors duration-300"
            :class="isAnimating ? 'bg-amber-500 animate-pulse' : 'bg-emerald-500'"
          />
          {{ phaseLabel }}
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 xl:grid-cols-[1fr_380px] gap-6">
      <!-- Left: topology + controls -->
      <section class="space-y-4">
        <!-- Topology -->
        <div
          class="relative rounded-2xl border border-slate-200 bg-white p-6 md:p-8 overflow-hidden shadow-sm"
          aria-label="网络拓扑"
        >
          <div
            class="absolute inset-0 opacity-40 pointer-events-none"
            style="
              background-image: radial-gradient(circle at 1px 1px, #cbd5e1 1px, transparent 0);
              background-size: 24px 24px;
            "
          />

          <div class="relative flex items-stretch justify-between gap-4 min-h-[220px]">
            <!-- Client node -->
            <article
              class="flex-1 max-w-[200px] flex flex-col items-center z-10"
              aria-label="客户端"
            >
              <div
                class="w-full rounded-xl border-2 p-4 transition-all duration-500"
                :class="nodeClass('client')"
              >
                <div class="flex items-center gap-2 mb-2">
                  <Monitor class="w-5 h-5 text-teal-600" />
                  <span class="font-semibold text-sm text-slate-800">Client</span>
                </div>
                <p class="text-[10px] text-slate-500 font-mono truncate" :title="clientIp">
                  {{ clientMac }}
                </p>
                <p class="text-[10px] text-slate-600 font-mono">{{ clientIp }}</p>
              </div>
              <div
                class="mt-3 px-3 py-1.5 rounded-lg text-xs font-mono font-medium border transition-all duration-500"
                :class="stateBadgeClass(clientState)"
              >
                {{ clientState }}
              </div>
            </article>

            <!-- Link + animated packet -->
            <div class="flex-1 flex flex-col justify-center relative min-w-[120px] mx-2">
              <svg
                class="absolute inset-0 w-full h-full pointer-events-none"
                preserveAspectRatio="none"
                viewBox="0 0 100 40"
              >
                <line
                  x1="0"
                  y1="20"
                  x2="100"
                  y2="20"
                  stroke="currentColor"
                  class="text-slate-300"
                  stroke-width="0.5"
                  stroke-dasharray="2 2"
                />
              </svg>

              <!-- Packet bubble -->
              <Transition name="packet-pop">
                <div
                  v-if="activePacket && isAnimating"
                  class="absolute top-1/2 -translate-y-1/2 z-20 pointer-events-none"
                  :style="packetStyle"
                >
                  <div
                    class="px-2.5 py-1 rounded-md text-[10px] font-mono font-bold shadow-lg border whitespace-nowrap"
                    :class="packetBubbleClass"
                  >
                    {{ activePacket.label }}
                  </div>
                </div>
              </Transition>

              <p
                v-if="!isAnimating && !activePacket"
                class="text-center text-[10px] text-slate-400 font-mono absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2"
              >
                — 链路空闲 —
              </p>
            </div>

            <!-- Server node -->
            <article
              class="flex-1 max-w-[200px] flex flex-col items-center z-10"
              aria-label="服务器"
            >
              <div
                class="w-full rounded-xl border-2 p-4 transition-all duration-500"
                :class="nodeClass('server')"
              >
                <div class="flex items-center gap-2 mb-2">
                  <Server class="w-5 h-5 text-indigo-600" />
                  <span class="font-semibold text-sm text-slate-800">Server</span>
                </div>
                <p class="text-[10px] text-slate-500 font-mono truncate" :title="serverIp">
                  {{ serverMac }}
                </p>
                <p class="text-[10px] text-slate-600 font-mono">{{ serverIp }}</p>
              </div>
              <div
                class="mt-3 px-3 py-1.5 rounded-lg text-xs font-mono font-medium border transition-all duration-500"
                :class="stateBadgeClass(serverState)"
              >
                {{ serverState }}
              </div>
            </article>
          </div>

          <!-- Seq timeline hint -->
          <div
            v-if="mode !== 'idle'"
            class="mt-6 flex flex-wrap gap-4 text-[10px] font-mono text-slate-500 border-t border-slate-100 pt-4"
          >
            <span>client ISN: <strong class="text-teal-700">{{ clientIsn }}</strong></span>
            <span>server ISN: <strong class="text-indigo-700">{{ serverIsn }}</strong></span>
            <span v-if="clientNextSeq">client next: {{ clientNextSeq }}</span>
            <span v-if="serverNextSeq">server next: {{ serverNextSeq }}</span>
          </div>
        </div>

        <!-- Controls -->
        <div
          class="flex flex-wrap gap-2 p-4 rounded-xl border border-slate-200 bg-white shadow-sm"
          role="toolbar"
          aria-label="协议控制"
        >
          <button
            type="button"
            class="btn-primary"
            :disabled="controlsDisabled || mode === 'handshake'"
            @click="startHandshake"
          >
            开始握手
          </button>
          <button
            type="button"
            class="btn-secondary"
            :disabled="controlsDisabled || !canNextStep"
            @click="nextStep"
          >
            下一步
            <span class="text-slate-500 ml-1">({{ stepDisplay }})</span>
          </button>
          <button
            type="button"
            class="btn-secondary"
            :disabled="controlsDisabled || mode !== 'established'"
            @click="startWave"
          >
            开始挥手
          </button>
          <button type="button" class="btn-ghost" :disabled="isAnimating" @click="resetAll">
            重置
          </button>
        </div>

        <!-- Step progress -->
        <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <h2 class="text-xs font-medium text-slate-500 uppercase tracking-wider mb-3">
            当前阶段进度
          </h2>
          <div class="flex gap-1">
            <div
              v-for="(s, i) in progressSteps"
              :key="i"
              class="h-1.5 flex-1 rounded-full transition-all duration-300"
              :class="
                i < currentStepIndex
                  ? 'bg-teal-500'
                  : i === currentStepIndex && mode !== 'idle' && mode !== 'established'
                    ? 'bg-teal-300 animate-pulse'
                    : 'bg-slate-200'
              "
              :title="s"
            />
          </div>
          <p class="text-xs text-slate-500 mt-2 font-mono">{{ progressLabel }}</p>
        </div>
      </section>

      <!-- Right: logs + packet detail -->
      <aside class="space-y-4 flex flex-col min-h-0">
        <!-- Packet detail panel -->
        <div
          class="rounded-2xl border border-slate-200 bg-white p-4 shrink-0 shadow-sm"
          aria-label="报文详情"
        >
          <h2 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
            <FileCode class="w-4 h-4 text-teal-600" />
            报文详情面板
          </h2>
          <template v-if="currentDetail">
            <dl class="grid grid-cols-[auto_1fr] gap-x-3 gap-y-2 text-xs font-mono">
              <dt class="text-slate-500">步骤</dt>
              <dd class="text-slate-800">
                #{{ currentDetail.stepNum }} — {{ currentDetail.stepName }}
              </dd>
              <dt class="text-slate-500">方向</dt>
              <dd :class="currentDetail.direction === 'C→S' ? 'text-teal-700' : 'text-indigo-700'">
                {{ currentDetail.direction }}
              </dd>
              <dt class="text-slate-500">源 MAC</dt>
              <dd class="text-slate-700 truncate">{{ currentDetail.srcMac }}</dd>
              <dt class="text-slate-500">目的 MAC</dt>
              <dd class="text-slate-700 truncate">{{ currentDetail.dstMac }}</dd>
              <dt class="text-slate-500">源 IP</dt>
              <dd class="text-slate-700">{{ currentDetail.srcIp }}</dd>
              <dt class="text-slate-500">目的 IP</dt>
              <dd class="text-slate-700">{{ currentDetail.dstIp }}</dd>
              <dt class="text-slate-500">控制位</dt>
              <dd>
                <span
                  v-for="flag in currentDetail.flags"
                  :key="flag"
                  class="inline-block mr-1 px-1.5 py-0.5 rounded bg-amber-50 text-amber-800 border border-amber-200"
                >
                  {{ flag }}
                </span>
                <span v-if="!currentDetail.flags.length" class="text-slate-400">—</span>
              </dd>
              <dt class="text-slate-500">seq</dt>
              <dd class="text-emerald-700">{{ currentDetail.seq ?? '—' }}</dd>
              <dt class="text-slate-500">ack</dt>
              <dd class="text-emerald-700">{{ currentDetail.ack ?? '—' }}</dd>
            </dl>
          </template>
          <p v-else class="text-xs text-slate-400 font-mono">等待协议步骤执行…</p>
        </div>

        <!-- Step flow log -->
        <div
          class="rounded-2xl border border-slate-200 bg-white flex flex-col flex-1 min-h-[280px] overflow-hidden shadow-sm"
          aria-label="协议执行步骤流"
        >
          <h2 class="text-sm font-semibold text-slate-800 p-4 pb-2 flex items-center gap-2 shrink-0">
            <ListOrdered class="w-4 h-4 text-indigo-600" />
            协议执行步骤流
          </h2>
          <ol
            ref="logContainer"
            class="flex-1 overflow-y-auto px-4 pb-4 space-y-2 text-xs font-mono scroll-smooth"
          >
            <li
              v-for="(entry, idx) in logEntries"
              :key="idx"
              class="flex gap-2 p-2 rounded-lg border transition-colors"
              :class="
                idx === logEntries.length - 1
                  ? 'border-teal-300 bg-teal-50'
                  : 'border-slate-100 bg-slate-50'
              "
            >
              <span class="text-slate-400 shrink-0 w-6">#{{ entry.stepNum }}</span>
              <div class="min-w-0">
                <p class="text-slate-800">{{ entry.stepName }}</p>
                <p class="text-slate-500 mt-0.5 truncate">{{ entry.summary }}</p>
              </div>
            </li>
            <li v-if="!logEntries.length" class="text-slate-400 py-4 text-center">
              点击「开始握手」或「开始挥手」后，使用「下一步」单步推进
            </li>
          </ol>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { Monitor, Server, FileCode, ListOrdered } from 'lucide-vue-next'

/** @typedef {'CLOSED'|'LISTEN'|'SYN-SENT'|'SYN-RCVD'|'ESTABLISHED'|'FIN-WAIT-1'|'FIN-WAIT-2'|'CLOSE-WAIT'|'LAST-ACK'|'TIME-WAIT'} TcpState */

const clientMac = '00:1A:2B:3C:4D:5E'
const serverMac = '00:AA:BB:CC:DD:EE'
const clientIp = '192.168.1.100:52431'
const serverIp = '203.0.113.50:443'

const ANIM_MS = 900

const mode = ref('idle') // idle | handshake | established | wave | done
const currentStepIndex = ref(-1)
const clientState = ref('CLOSED')
const serverState = ref('LISTEN')

const clientIsn = ref(1000)
const serverIsn = ref(5000)
const clientNextSeq = ref(null)
const serverNextSeq = ref(null)

const isAnimating = ref(false)
const activePacket = ref(null)
const packetProgress = ref(0)
const logEntries = ref([])
const currentDetail = ref(null)
const logContainer = ref(null)

let animFrame = null

const phaseLabel = computed(() => {
  const map = {
    idle: '空闲',
    handshake: '三次握手',
    established: '连接已建立',
    wave: '四次挥手',
    done: '连接已关闭',
  }
  return map[mode.value] ?? mode.value
})

const controlsDisabled = computed(() => isAnimating.value)

const canNextStep = computed(() => {
  if (isAnimating.value) return false
  if (mode.value === 'handshake') return currentStepIndex.value < handshakeSteps.length - 1
  if (mode.value === 'wave') return currentStepIndex.value < waveSteps.length - 1
  return false
})

const stepDisplay = computed(() => {
  if (mode.value === 'handshake') {
    return `${Math.max(0, currentStepIndex.value + 1)}/${handshakeSteps.length}`
  }
  if (mode.value === 'wave') {
    return `${Math.max(0, currentStepIndex.value + 1)}/${waveSteps.length}`
  }
  return '—'
})

const progressSteps = computed(() => {
  if (mode.value === 'handshake') return handshakeSteps.map((s) => s.name)
  if (mode.value === 'wave') return waveSteps.map((s) => s.name)
  return []
})

const progressLabel = computed(() => {
  const steps =
    mode.value === 'handshake' ? handshakeSteps : mode.value === 'wave' ? waveSteps : []
  if (currentStepIndex.value < 0 || !steps.length) return '尚未开始'
  const cur = steps[currentStepIndex.value]
  return cur ? `${cur.name}` : '完成'
})

const packetStyle = computed(() => {
  const p = packetProgress.value
  const isC2S = activePacket.value?.direction === 'c2s'
  const left = isC2S ? 8 + p * 72 : 80 - p * 72
  return {
    left: `${left}%`,
    transition: 'none',
  }
})

const packetBubbleClass = computed(() => {
  const dir = activePacket.value?.direction
  return dir === 'c2s'
    ? 'bg-teal-50 border-teal-300 text-teal-800 shadow-md'
    : 'bg-indigo-50 border-indigo-300 text-indigo-800 shadow-md'
})

function nodeClass(side) {
  const highlight =
    (side === 'client' && activePacket.value?.direction === 'c2s' && isAnimating.value) ||
    (side === 'server' && activePacket.value?.direction === 's2c' && isAnimating.value)
  return highlight
    ? 'border-teal-400 bg-teal-50/50 shadow-md shadow-teal-100'
    : 'border-slate-200 bg-white'
}

function stateBadgeClass(state) {
  if (state === 'ESTABLISHED') return 'border-emerald-300 bg-emerald-50 text-emerald-800'
  if (state === 'CLOSED' || state === 'LISTEN')
    return 'border-slate-200 bg-slate-100 text-slate-600'
  if (state.includes('WAIT') || state === 'TIME-WAIT')
    return 'border-amber-300 bg-amber-50 text-amber-800'
  if (state.includes('SYN') || state === 'LAST-ACK')
    return 'border-teal-300 bg-teal-50 text-teal-800'
  if (state === 'CLOSE-WAIT') return 'border-indigo-300 bg-indigo-50 text-indigo-800'
  return 'border-slate-200 bg-slate-100 text-slate-700'
}

function buildDetail(step, packet, extra = {}) {
  const c2s = packet?.direction === 'c2s'
  return {
    stepNum: step.stepNum,
    stepName: step.name,
    direction: packet ? (c2s ? 'C→S' : 'S→C') : '—',
    srcMac: packet ? (c2s ? clientMac : serverMac) : '—',
    dstMac: packet ? (c2s ? serverMac : clientMac) : '—',
    srcIp: packet ? (c2s ? clientIp : serverIp) : '—',
    dstIp: packet ? (c2s ? serverIp : clientIp) : '—',
    flags: packet?.flags ?? [],
    seq: packet?.seq ?? null,
    ack: packet?.ack ?? null,
    ...extra,
  }
}

function appendLog(step, packet, summary) {
  logEntries.value.push({
    stepNum: step.stepNum,
    stepName: step.name,
    summary: summary ?? step.desc,
  })
  currentDetail.value = buildDetail(step, packet)
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
}

function runPacketAnimation(packet, onComplete) {
  activePacket.value = packet
  isAnimating.value = true
  packetProgress.value = 0
  const start = performance.now()

  function frame(now) {
    const t = Math.min(1, (now - start) / ANIM_MS)
    packetProgress.value = easeInOutCubic(t)
    if (t < 1) {
      animFrame = requestAnimationFrame(frame)
    } else {
      isAnimating.value = false
      activePacket.value = null
      packetProgress.value = 0
      onComplete()
    }
  }
  if (animFrame) cancelAnimationFrame(animFrame)
  animFrame = requestAnimationFrame(frame)
}

function easeInOutCubic(t) {
  return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
}

/** 三次握手步骤（RFC 793） */
const handshakeSteps = [
  {
    stepNum: 1,
    name: '第一次握手',
    desc: '客户端发送 SYN，进入 SYN-SENT',
    packet: { direction: 'c2s', label: 'SYN', flags: ['SYN'], seq: () => clientIsn.value, ack: null },
    apply() {
      clientState.value = 'SYN-SENT'
      clientNextSeq.value = clientIsn.value + 1
    },
    summary(p) {
      return `SYN seq=${p.seq} → Server (仍为 LISTEN)`
    },
  },
  {
    stepNum: 2,
    name: '第二次握手',
    desc: '服务器收到 SYN，回复 SYN+ACK，进入 SYN-RCVD',
    packet: {
      direction: 's2c',
      label: 'SYN+ACK',
      flags: ['SYN', 'ACK'],
      seq: () => serverIsn.value,
      ack: () => clientIsn.value + 1,
    },
    apply() {
      serverState.value = 'SYN-RCVD'
      serverNextSeq.value = serverIsn.value + 1
    },
    summary(p) {
      return `SYN+ACK seq=${p.seq} ack=${p.ack} ← Client`
    },
  },
  {
    stepNum: 3,
    name: '第三次握手',
    desc: '客户端发送 ACK，双方进入 ESTABLISHED',
    packet: {
      direction: 'c2s',
      label: 'ACK',
      flags: ['ACK'],
      seq: () => clientIsn.value + 1,
      ack: () => serverIsn.value + 1,
    },
    apply() {
      clientState.value = 'ESTABLISHED'
    },
    summary(p) {
      return `ACK seq=${p.seq} ack=${p.ack} → Server`
    },
  },
  {
    stepNum: 4,
    name: '握手完成',
    desc: '服务器收到 ACK，进入 ESTABLISHED',
    packet: null,
    apply() {
      serverState.value = 'ESTABLISHED'
      mode.value = 'established'
    },
    summary() {
      return '双方状态均为 ESTABLISHED，连接建立'
    },
  },
]

/** 四次挥手步骤 */
const waveSteps = [
  {
    stepNum: 1,
    name: '第一次挥手',
    desc: '客户端发送 FIN，进入 FIN-WAIT-1',
    packet: {
      direction: 'c2s',
      label: 'FIN',
      flags: ['FIN', 'ACK'],
      seq: () => clientNextSeq.value,
      ack: () => serverNextSeq.value,
    },
    apply() {
      clientState.value = 'FIN-WAIT-1'
      const finSeq = clientNextSeq.value
      clientNextSeq.value = finSeq + 1
    },
    summary(p) {
      return `FIN seq=${p.seq} → Server`
    },
  },
  {
    stepNum: 2,
    name: '第二次挥手',
    desc: '服务器 ACK 客户端 FIN，进入 CLOSE-WAIT',
    packet: {
      direction: 's2c',
      label: 'ACK',
      flags: ['ACK'],
      seq: () => serverNextSeq.value,
      ack: () => clientNextSeq.value,
    },
    apply() {
      serverState.value = 'CLOSE-WAIT'
    },
    summary(p) {
      return `ACK ack=${p.ack} ← Client (Server → CLOSE-WAIT)`
    },
  },
  {
    stepNum: 3,
    name: '客户端半关闭',
    desc: '客户端收到 ACK，进入 FIN-WAIT-2',
    packet: null,
    apply() {
      clientState.value = 'FIN-WAIT-2'
    },
    summary() {
      return 'Client: FIN-WAIT-1 → FIN-WAIT-2'
    },
  },
  {
    stepNum: 4,
    name: '第三次挥手',
    desc: '服务器发送 FIN，进入 LAST-ACK',
    packet: {
      direction: 's2c',
      label: 'FIN',
      flags: ['FIN', 'ACK'],
      seq: () => serverNextSeq.value,
      ack: () => clientNextSeq.value,
    },
    apply() {
      serverState.value = 'LAST-ACK'
      serverNextSeq.value = serverNextSeq.value + 1
    },
    summary(p) {
      return `FIN seq=${p.seq} ← Client (Server → LAST-ACK)`
    },
  },
  {
    stepNum: 5,
    name: '第四次挥手',
    desc: '客户端 ACK 服务器 FIN，进入 TIME-WAIT',
    packet: {
      direction: 'c2s',
      label: 'ACK',
      flags: ['ACK'],
      seq: () => clientNextSeq.value,
      ack: () => serverNextSeq.value,
    },
    apply() {
      clientState.value = 'TIME-WAIT'
    },
    summary(p) {
      return `ACK ack=${p.ack} → Server (Client → TIME-WAIT)`
    },
  },
  {
    stepNum: 6,
    name: '挥手完成',
    desc: '服务器收到 ACK 关闭；客户端 2MSL 后 CLOSED',
    packet: null,
    apply() {
      serverState.value = 'CLOSED'
      clientState.value = 'CLOSED'
      mode.value = 'done'
    },
    summary() {
      return 'Server: CLOSED · Client: TIME-WAIT → CLOSED (2MSL)'
    },
  },
]

function resolvePacket(raw) {
  if (!raw) return null
  return {
    direction: raw.direction,
    label: raw.label,
    flags: [...raw.flags],
    seq: typeof raw.seq === 'function' ? raw.seq() : raw.seq,
    ack: typeof raw.ack === 'function' ? raw.ack() : raw.ack,
  }
}

function executeStep(steps) {
  const nextIdx = currentStepIndex.value + 1
  if (nextIdx >= steps.length) return

  const step = steps[nextIdx]
  const packet = resolvePacket(step.packet)

  const finish = () => {
    step.apply()
    appendLog(step, packet, packet ? step.summary(packet) : step.summary())
    currentStepIndex.value = nextIdx
  }

  if (packet) {
    runPacketAnimation(packet, finish)
  } else {
    finish()
  }
}

function startHandshake() {
  resetAll(false)
  mode.value = 'handshake'
  currentStepIndex.value = -1
  clientState.value = 'CLOSED'
  serverState.value = 'LISTEN'
  clientIsn.value = 1000
  serverIsn.value = 5000
  clientNextSeq.value = null
  serverNextSeq.value = null
  logEntries.value.push({
    stepNum: 0,
    stepName: '准备握手',
    summary: 'Client=CLOSED, Server=LISTEN，点击「下一步」发送 SYN',
  })
}

function startWave() {
  if (mode.value !== 'established') return
  mode.value = 'wave'
  currentStepIndex.value = -1
  logEntries.value.push({
    stepNum: 0,
    stepName: '准备挥手',
    summary: '双方 ESTABLISHED，客户端主动关闭连接',
  })
}

function nextStep() {
  if (mode.value === 'handshake') executeStep(handshakeSteps)
  else if (mode.value === 'wave') executeStep(waveSteps)
}

function resetAll(clearLogs = true) {
  if (animFrame) cancelAnimationFrame(animFrame)
  mode.value = 'idle'
  currentStepIndex.value = -1
  clientState.value = 'CLOSED'
  serverState.value = 'LISTEN'
  clientIsn.value = 1000
  serverIsn.value = 5000
  clientNextSeq.value = null
  serverNextSeq.value = null
  isAnimating.value = false
  activePacket.value = null
  packetProgress.value = 0
  currentDetail.value = null
  if (clearLogs) logEntries.value = []
}

watch(logEntries, () => {
  nextTick(() => {
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
    }
  })
})
</script>

<style scoped>
.btn-primary {
  @apply px-4 py-2 rounded-lg text-sm font-medium bg-teal-600 hover:bg-teal-500 text-white shadow-sm
    disabled:opacity-40 disabled:cursor-not-allowed transition-colors duration-150;
}
.btn-secondary {
  @apply px-4 py-2 rounded-lg text-sm font-medium bg-white hover:bg-slate-50
    border border-slate-300 text-slate-700 disabled:opacity-40 disabled:cursor-not-allowed
    transition-colors duration-150 shadow-sm;
}
.btn-ghost {
  @apply px-4 py-2 rounded-lg text-sm font-medium text-slate-500 hover:text-slate-800
    hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors duration-150;
}

.packet-pop-enter-active {
  animation: packet-pop-in 0.2s ease-out;
}
@keyframes packet-pop-in {
  from {
    opacity: 0;
    transform: translateY(-50%) scale(0.85);
  }
  to {
    opacity: 1;
    transform: translateY(-50%) scale(1);
  }
}
</style>
