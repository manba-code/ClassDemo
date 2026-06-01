<template>
  <div class="text-slate-800 font-sans selection:bg-indigo-200/60 pb-8">
    <header class="border-b border-slate-200 bg-white/90 backdrop-blur-sm px-6 py-4 shadow-sm">
      <div class="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="text-xl font-semibold tracking-tight text-slate-900">路由转发</h1>
          <p class="text-sm text-slate-500 mt-0.5">跨网段 IP 数据报转发 · 路由表查表与下一跳选择</p>
        </div>
        <div
          class="flex items-center gap-2 text-xs font-mono px-3 py-1.5 rounded-full border border-slate-200 bg-white text-slate-600 shadow-sm"
        >
          <span
            class="w-2 h-2 rounded-full transition-colors duration-300"
            :class="isAnimating ? 'bg-amber-500 animate-pulse' : mode === 'done' ? 'bg-emerald-500' : 'bg-slate-400'"
          />
          {{ phaseLabel }}
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 xl:grid-cols-[1fr_360px] gap-6">
      <section class="space-y-4">
        <!-- Topology -->
        <div
          class="relative rounded-2xl border border-slate-200 bg-white p-4 md:p-6 overflow-hidden shadow-sm"
          aria-label="网络拓扑"
        >
          <div
            class="absolute inset-0 opacity-40 pointer-events-none"
            style="
              background-image: radial-gradient(circle at 1px 1px, #cbd5e1 1px, transparent 0);
              background-size: 24px 24px;
            "
          />

          <!-- Segment labels -->
          <div class="relative flex justify-between text-[10px] font-mono text-slate-400 mb-2 px-2">
            <span class="flex-1 text-center">网段 A · 192.168.1.0/24</span>
            <span class="flex-1 text-center">网段 B · 10.0.0.0/24</span>
            <span class="flex-1 text-center">网段 C · 172.16.0.0/24</span>
          </div>

          <div class="relative flex items-center justify-between gap-1 min-h-[200px] px-1">
            <template v-for="(node, idx) in nodes" :key="node.id">
              <article
                class="flex flex-col items-center z-10 shrink-0"
                :class="nodeWidthClass(node.type)"
                :aria-label="node.label"
              >
                <div
                  class="w-full rounded-xl border-2 p-3 transition-all duration-500"
                  :class="nodeBoxClass(node.id)"
                >
                  <div class="flex items-center gap-1.5 mb-1.5">
                    <component :is="node.icon" class="w-4 h-4" :class="node.iconColor" />
                    <span class="font-semibold text-xs text-slate-800">{{ node.label }}</span>
                  </div>
                  <p class="text-[10px] text-slate-600 font-mono">{{ node.ip }}</p>
                  <p v-if="node.extra" class="text-[9px] text-slate-400 font-mono mt-0.5">{{ node.extra }}</p>
                </div>
              </article>

              <!-- Link between nodes -->
              <div
                v-if="idx < nodes.length - 1"
                class="flex-1 relative min-w-[40px] h-12 mx-0.5"
              >
                <svg class="absolute inset-0 w-full h-full" preserveAspectRatio="none" viewBox="0 0 100 20">
                  <line
                    x1="0"
                    y1="10"
                    x2="100"
                    y2="10"
                    stroke="currentColor"
                    class="transition-colors duration-300"
                    :class="linkClass(idx)"
                    stroke-width="1"
                    :stroke-dasharray="activeLinkIndex === idx && isAnimating ? 'none' : '3 3'"
                  />
                </svg>
                <Transition name="packet-pop">
                  <div
                    v-if="activePacket && isAnimating && activeLinkIndex === idx"
                    class="absolute top-1/2 -translate-y-1/2 z-20 pointer-events-none"
                    :style="packetStyle"
                  >
                    <div class="px-2 py-0.5 rounded-md text-[9px] font-mono font-bold shadow-lg border bg-indigo-50 border-indigo-300 text-indigo-800 whitespace-nowrap">
                      IP 包
                    </div>
                  </div>
                </Transition>
              </div>
            </template>
          </div>

          <!-- Packet info bar -->
          <div
            v-if="mode !== 'idle'"
            class="relative mt-4 flex flex-wrap gap-3 text-[10px] font-mono text-slate-500 border-t border-slate-100 pt-3"
          >
            <span>源 IP: <strong class="text-teal-700">{{ srcIp }}</strong></span>
            <span>目的 IP: <strong class="text-indigo-700">{{ dstIp }}</strong></span>
            <span v-if="currentHop">当前处理: <strong class="text-slate-700">{{ currentHop }}</strong></span>
          </div>
        </div>

        <!-- Controls -->
        <div
          class="flex flex-wrap gap-2 p-4 rounded-xl border border-slate-200 bg-white shadow-sm"
          role="toolbar"
        >
          <button type="button" class="btn-primary" :disabled="controlsDisabled || mode === 'running'" @click="start">
            开始
          </button>
          <button type="button" class="btn-secondary" :disabled="controlsDisabled || !canNextStep" @click="nextStep">
            下一步
            <span class="text-slate-500 ml-1">({{ stepDisplay }})</span>
          </button>
          <button type="button" class="btn-ghost" :disabled="isAnimating" @click="resetAll">
            重置
          </button>
        </div>

        <!-- Result -->
        <Transition name="result-fade">
          <div
            v-if="mode === 'done'"
            class="rounded-xl border-2 border-emerald-300 bg-emerald-50 p-5 shadow-sm"
            role="status"
          >
            <div class="flex items-start gap-3">
              <CheckCircle2 class="w-6 h-6 text-emerald-600 shrink-0" />
              <div>
                <p class="text-base font-semibold text-emerald-900">转发完成</p>
                <p class="text-sm text-emerald-700 mt-1 font-mono">
                  {{ srcIp }} → {{ dstIp }}，途经 R1、R2，H2 成功收到数据报
                </p>
                <p class="text-xs text-emerald-600/80 mt-2">
                  路径：H1 → R1（查表下一跳 10.0.0.2）→ R2（查表直连交付）→ H2
                </p>
              </div>
            </div>
          </div>
        </Transition>

        <!-- Routing tables -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <RoutingTablePanel
            title="R1 路由表"
            router-label="192.168.1.1 / 10.0.0.1"
            :rows="r1Table"
            :highlight-index="activeRouter === 'r1' ? highlightRow : -1"
            :is-active="activeRouter === 'r1'"
          />
          <RoutingTablePanel
            title="R2 路由表"
            router-label="10.0.0.2 / 172.16.0.1"
            :rows="r2Table"
            :highlight-index="activeRouter === 'r2' ? highlightRow : -1"
            :is-active="activeRouter === 'r2'"
          />
        </div>
      </section>

      <!-- Right panel -->
      <aside class="space-y-4 flex flex-col min-h-0">
        <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm shrink-0">
          <h2 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
            <FileCode class="w-4 h-4 text-indigo-600" />
            当前步骤详情
          </h2>
          <template v-if="currentStep">
            <dl class="grid grid-cols-[auto_1fr] gap-x-3 gap-y-2 text-xs font-mono">
              <dt class="text-slate-500">步骤</dt>
              <dd class="text-slate-800">#{{ currentStep.stepNum }} — {{ currentStep.name }}</dd>
              <dt class="text-slate-500">处理设备</dt>
              <dd class="text-slate-700">{{ currentStep.device ?? '—' }}</dd>
              <dt class="text-slate-500">查表结果</dt>
              <dd class="text-indigo-700">{{ currentStep.lookup ?? '—' }}</dd>
              <dt class="text-slate-500">下一跳</dt>
              <dd class="text-emerald-700">{{ currentStep.nextHop ?? '—' }}</dd>
              <dt class="text-slate-500">出接口</dt>
              <dd class="text-slate-700">{{ currentStep.iface ?? '—' }}</dd>
            </dl>
            <p class="text-xs text-slate-500 mt-3 leading-relaxed font-sans">{{ currentStep.desc }}</p>
          </template>
          <p v-else class="text-xs text-slate-400 font-mono">点击「开始」后使用「下一步」推进</p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white flex flex-col flex-1 min-h-[260px] overflow-hidden shadow-sm">
          <h2 class="text-sm font-semibold text-slate-800 p-4 pb-2 flex items-center gap-2 shrink-0">
            <ListOrdered class="w-4 h-4 text-indigo-600" />
            转发步骤
          </h2>
          <ol ref="logContainer" class="flex-1 overflow-y-auto px-4 pb-4 space-y-2 text-xs font-mono scroll-smooth">
            <li
              v-for="(entry, idx) in logEntries"
              :key="idx"
              class="flex gap-2 p-2 rounded-lg border transition-colors"
              :class="idx === logEntries.length - 1 ? 'border-indigo-300 bg-indigo-50' : 'border-slate-100 bg-slate-50'"
            >
              <span class="text-slate-400 shrink-0 w-6">#{{ entry.stepNum }}</span>
              <div class="min-w-0">
                <p class="text-slate-800">{{ entry.stepName }}</p>
                <p class="text-slate-500 mt-0.5">{{ entry.summary }}</p>
              </div>
            </li>
          </ol>
        </div>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import {
  Monitor,
  Server,
  Router,
  FileCode,
  ListOrdered,
  CheckCircle2,
} from 'lucide-vue-next'
import RoutingTablePanel from './RoutingTablePanel.vue'

const srcIp = '192.168.1.10'
const dstIp = '172.16.0.20'
const ANIM_MS = 850

const nodes = [
  { id: 'h1', label: 'H1', ip: '192.168.1.10', type: 'host', icon: Monitor, iconColor: 'text-teal-600' },
  {
    id: 'r1',
    label: 'R1',
    ip: '192.168.1.1',
    extra: 'eth1: 10.0.0.1',
    type: 'router',
    icon: Router,
    iconColor: 'text-amber-600',
  },
  {
    id: 'r2',
    label: 'R2',
    ip: '10.0.0.2',
    extra: 'eth1: 172.16.0.1',
    type: 'router',
    icon: Router,
    iconColor: 'text-amber-600',
  },
  { id: 'h2', label: 'H2', ip: '172.16.0.20', type: 'host', icon: Server, iconColor: 'text-indigo-600' },
]

const r1Table = [
  { network: '192.168.1.0', mask: '/24', nextHop: '直连', iface: 'eth0', type: '直连' },
  { network: '10.0.0.0', mask: '/24', nextHop: '直连', iface: 'eth1', type: '直连' },
  { network: '172.16.0.0', mask: '/24', nextHop: '10.0.0.2', iface: 'eth1', type: '静态' },
]

const r2Table = [
  { network: '172.16.0.0', mask: '/24', nextHop: '直连', iface: 'eth1', type: '直连' },
  { network: '10.0.0.0', mask: '/24', nextHop: '直连', iface: 'eth0', type: '直连' },
  { network: '192.168.1.0', mask: '/24', nextHop: '10.0.0.1', iface: 'eth0', type: '静态' },
]

const mode = ref('idle')
const currentStepIndex = ref(-1)
const isAnimating = ref(false)
const activeLinkIndex = ref(-1)
const packetProgress = ref(0)
const activeRouter = ref(null)
const highlightRow = ref(-1)
const currentHop = ref('')
const logEntries = ref([])
const logContainer = ref(null)

let animFrame = null

const steps = [
  {
    stepNum: 1,
    name: 'H1 发送数据报',
    desc: 'H1 构造 IP 数据报（目的 172.16.0.20），不在本地网段，默认网关为 R1（192.168.1.1），将帧发往 R1。',
    device: 'H1',
    lookup: '目的不在 192.168.1.0/24，查默认网关',
    nextHop: '192.168.1.1 (R1)',
    iface: '本地网卡',
    linkIndex: 0,
    packetFrom: 'h1',
    apply() {
      currentHop.value = 'H1 → R1'
      activeRouter.value = null
      highlightRow.value = -1
    },
    summary: () => `H1 将 IP 包发往默认网关 R1`,
  },
  {
    stepNum: 2,
    name: 'R1 查路由表',
    desc: 'R1 收到数据报，提取目的地址 172.16.0.20，在路由表中最长前缀匹配 172.16.0.0/24，下一跳为 10.0.0.2（R2）。',
    device: 'R1',
    lookup: '172.16.0.0/24 匹配',
    nextHop: '10.0.0.2 (R2)',
    iface: 'eth1',
    linkIndex: 1,
    packetFrom: 'r1',
    apply() {
      currentHop.value = 'R1 查表转发'
      activeRouter.value = 'r1'
      highlightRow.value = 2
    },
    summary: () => 'R1 匹配 172.16.0.0/24，下一跳 10.0.0.2',
  },
  {
    stepNum: 3,
    name: 'R1 → R2 转发',
    desc: 'R1 将 IP 数据报从 eth1 发出，经网段 B 送达 R2。',
    device: 'R1',
    lookup: '—',
    nextHop: '10.0.0.2',
    iface: 'eth1',
    linkIndex: 1,
    packetFrom: 'r1',
    apply() {
      currentHop.value = 'R1 → R2'
      activeRouter.value = 'r1'
      highlightRow.value = 2
    },
    summary: () => '数据报经 10.0.0.0/24 网段到达 R2',
  },
  {
    stepNum: 4,
    name: 'R2 查路由表',
    desc: 'R2 收到数据报，目的 172.16.0.20 匹配直连网络 172.16.0.0/24，可直接从 eth1 交付。',
    device: 'R2',
    lookup: '172.16.0.0/24 直连',
    nextHop: '直连交付',
    iface: 'eth1',
    linkIndex: 2,
    packetFrom: 'r2',
    apply() {
      currentHop.value = 'R2 查表转发'
      activeRouter.value = 'r2'
      highlightRow.value = 0
    },
    summary: () => 'R2 匹配直连网段 172.16.0.0/24',
  },
  {
    stepNum: 5,
    name: 'R2 → H2 转发',
    desc: 'R2 将数据报从 eth1 发出，送达同一网段内的 H2。',
    device: 'R2',
    lookup: '—',
    nextHop: '172.16.0.20 (H2)',
    iface: 'eth1',
    linkIndex: 2,
    packetFrom: 'r2',
    apply() {
      currentHop.value = 'R2 → H2'
      activeRouter.value = 'r2'
      highlightRow.value = 0
    },
    summary: () => '数据报经 172.16.0.0/24 网段到达 H2',
  },
  {
    stepNum: 6,
    name: 'H2 接收数据报',
    desc: 'H2 收到目的地址为本机 IP 的数据报，转发过程结束。',
    device: 'H2',
    lookup: '目的 IP = 本机',
    nextHop: '—',
    iface: '—',
    linkIndex: null,
    packetFrom: null,
    apply() {
      currentHop.value = 'H2 已接收'
      activeRouter.value = null
      highlightRow.value = -1
      mode.value = 'done'
    },
    summary: () => `${dstIp} 成功收到来自 ${srcIp} 的数据报`,
  },
]

const activePacket = computed(() => isAnimating.value && activeLinkIndex.value >= 0)

const phaseLabel = computed(() => {
  const map = { idle: '空闲', running: '转发进行中', done: '转发完成' }
  return map[mode.value] ?? mode.value
})

const controlsDisabled = computed(() => isAnimating.value)
const canNextStep = computed(
  () => !isAnimating.value && mode.value === 'running' && currentStepIndex.value < steps.length - 1
)
const stepDisplay = computed(() =>
  mode.value === 'running' ? `${Math.max(0, currentStepIndex.value + 1)}/${steps.length}` : '—'
)
const currentStep = computed(() =>
  currentStepIndex.value >= 0 ? steps[currentStepIndex.value] : null
)

const packetStyle = computed(() => ({
  left: `${8 + packetProgress.value * 84}%`,
  transition: 'none',
}))

function nodeWidthClass(type) {
  return type === 'router' ? 'w-[88px] md:w-[100px]' : 'w-[76px] md:w-[88px]'
}

function nodeBoxClass(id) {
  const active = currentStep.value?.packetFrom === id && isAnimating.value
  const processing = currentStep.value?.device?.startsWith(id === 'h1' ? 'H1' : id === 'h2' ? 'H2' : id.toUpperCase()) && !isAnimating.value
  if (active) return 'border-indigo-400 bg-indigo-50/60 shadow-md shadow-indigo-100'
  if (processing) return 'border-amber-300 bg-amber-50/50'
  return 'border-slate-200 bg-white'
}

function linkClass(idx) {
  if (isAnimating.value && activeLinkIndex.value === idx) return 'text-indigo-500'
  if (currentStepIndex.value >= 0) {
    const step = steps[currentStepIndex.value]
    if (step.linkIndex === idx) return 'text-indigo-400'
  }
  return 'text-slate-300'
}

function appendLog(step) {
  logEntries.value.push({
    stepNum: step.stepNum,
    stepName: step.name,
    summary: step.summary(),
  })
  nextTick(() => {
    if (logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight
  })
}

function runAnimation(linkIndex, onComplete) {
  activeLinkIndex.value = linkIndex
  isAnimating.value = true
  packetProgress.value = 0
  const start = performance.now()

  function frame(now) {
    const t = Math.min(1, (now - start) / ANIM_MS)
    packetProgress.value = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
    if (t < 1) {
      animFrame = requestAnimationFrame(frame)
    } else {
      isAnimating.value = false
      activeLinkIndex.value = -1
      packetProgress.value = 0
      onComplete()
    }
  }
  if (animFrame) cancelAnimationFrame(animFrame)
  animFrame = requestAnimationFrame(frame)
}

function executeStep() {
  const nextIdx = currentStepIndex.value + 1
  if (nextIdx >= steps.length) return
  const step = steps[nextIdx]

  const finish = () => {
    step.apply()
    appendLog(step)
    currentStepIndex.value = nextIdx
  }

  if (step.linkIndex !== null && step.linkIndex !== undefined) {
    runAnimation(step.linkIndex, finish)
  } else {
    finish()
  }
}

function start() {
  resetAll(false)
  mode.value = 'running'
  currentStepIndex.value = -1
  logEntries.value.push({
    stepNum: 0,
    stepName: '准备转发',
    summary: `H1(${srcIp}) 向 H2(${dstIp}) 发送数据报，点击「下一步」开始`,
  })
}

function nextStep() {
  if (mode.value === 'running') executeStep()
}

function resetAll(clearLogs = true) {
  if (animFrame) cancelAnimationFrame(animFrame)
  mode.value = 'idle'
  currentStepIndex.value = -1
  isAnimating.value = false
  activeLinkIndex.value = -1
  packetProgress.value = 0
  activeRouter.value = null
  highlightRow.value = -1
  currentHop.value = ''
  if (clearLogs) logEntries.value = []
}

watch(logEntries, () => {
  nextTick(() => {
    if (logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight
  })
})
</script>

<style scoped>
.btn-primary {
  @apply px-4 py-2 rounded-lg text-sm font-medium bg-indigo-600 hover:bg-indigo-500 text-white shadow-sm
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
  from { opacity: 0; transform: translateY(-50%) scale(0.85); }
  to { opacity: 1; transform: translateY(-50%) scale(1); }
}
.result-fade-enter-active {
  animation: result-slide-in 0.35s ease-out;
}
@keyframes result-slide-in {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
