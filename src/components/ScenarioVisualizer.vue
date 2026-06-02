<template>
  <div class="text-slate-800 font-sans selection:bg-emerald-200/60 pb-8">
    <header class="border-b border-slate-200 bg-white/90 backdrop-blur-sm px-6 py-4 shadow-sm">
      <div class="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="text-xl font-semibold tracking-tight text-slate-900">综合网络场景模拟</h1>
          <p class="text-sm text-slate-500 mt-0.5">
            H1 访问 {{ meta.domain }} · t0 → t1 分步演示
          </p>
        </div>
        <div
          class="flex items-center gap-2 text-xs font-mono px-3 py-1.5 rounded-full border border-slate-200 bg-white text-slate-600 shadow-sm"
        >
          <span
            class="w-2 h-2 rounded-full transition-colors duration-300"
            :class="
              isAnimating
                ? 'bg-amber-500 animate-pulse'
                : mode === 'done'
                  ? 'bg-emerald-500'
                  : 'bg-slate-400'
            "
          />
          {{ phaseLabel }}
        </div>
      </div>
    </header>

    <main class="max-w-7xl mx-auto p-4 md:p-6 grid grid-cols-1 xl:grid-cols-[1fr_380px] gap-6">
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

          <p class="relative text-[10px] font-mono text-slate-400 text-center mb-3">
            局域网 192.168.1.0/24
          </p>

          <div class="relative flex flex-col items-center gap-2 min-h-[320px]">
            <!-- LAN hosts row -->
            <div class="flex flex-wrap justify-center gap-3 md:gap-6 w-full">
              <TopologyNode
                v-for="id in ['h1', 'h2', 'dns']"
                :key="id"
                :device="devices[id]"
                :icon="nodeIcons[id]"
                :active="isNodeActive(id)"
                :processing="isNodeProcessing(id)"
              />
            </div>

            <!-- vertical links to S -->
            <div class="flex justify-center gap-16 md:gap-24 h-6 w-full max-w-md">
              <div
                v-for="lid in ['h1-s', 'h2-s', 'dns-s']"
                :key="lid"
                class="w-px flex-1 max-w-[2px] self-stretch rounded-full transition-colors duration-300"
                :class="linkBarClass(lid)"
              />
            </div>

            <!-- Switch S -->
            <TopologyNode
              :device="devices.s"
              :icon="Network"
              :active="isNodeActive('s')"
              :processing="isNodeProcessing('s')"
              class="z-10"
            />

            <div
              class="w-px h-6 rounded-full transition-colors duration-300"
              :class="linkBarClass('s-r')"
            />

            <!-- Router -->
            <TopologyNode
              :device="devices.r"
              :icon="Router"
              :active="isNodeActive('r')"
              :processing="isNodeProcessing('r')"
            />

            <div
              class="w-px h-6 rounded-full transition-colors duration-300"
              :class="linkBarClass('r-inet')"
            />

            <!-- Internet -->
            <TopologyNode
              :device="devices.internet"
              :icon="Cloud"
              :active="isNodeActive('internet')"
              :processing="isNodeProcessing('internet')"
            />

            <div
              class="w-px h-6 rounded-full transition-colors duration-300"
              :class="linkBarClass('inet-web')"
            />

            <!-- Web Server -->
            <TopologyNode
              :device="devices.web"
              :icon="Globe"
              :active="isNodeActive('web')"
              :processing="isNodeProcessing('web')"
            />

            <!-- Animated packet -->
            <Transition name="packet-pop">
              <div
                v-if="isAnimating && activePacketLabel"
                class="absolute z-30 pointer-events-none"
                :style="packetBubbleStyle"
              >
                <div
                  class="px-2.5 py-1 rounded-md text-[10px] font-mono font-bold shadow-lg border whitespace-nowrap bg-emerald-50 border-emerald-400 text-emerald-800"
                >
                  {{ activePacketLabel }}
                </div>
              </div>
            </Transition>
          </div>

          <div
            v-if="mode !== 'idle'"
            class="relative mt-4 flex flex-wrap gap-3 text-[10px] font-mono text-slate-500 border-t border-slate-100 pt-3"
          >
            <span
              >t0：<strong class="text-slate-700">{{ meta.t0 }}</strong></span
            >
            <span v-if="mode === 'done'" class="text-emerald-700"
              >t1：<strong>{{ meta.t1 }}</strong></span
            >
          </div>
        </div>

        <!-- Controls -->
        <div
          class="flex flex-wrap gap-2 p-4 rounded-xl border border-slate-200 bg-white shadow-sm"
          role="toolbar"
        >
          <button
            type="button"
            class="btn-primary"
            :disabled="controlsDisabled || mode === 'running'"
            @click="start"
          >
            开始
          </button>
          <button type="button" class="btn-secondary" :disabled="!canPrev" @click="prevStep">
            上一步
          </button>
          <button type="button" class="btn-secondary" :disabled="!canNext" @click="nextStep">
            下一步
            <span class="text-slate-500 ml-1">({{ stepDisplay }})</span>
          </button>
          <button type="button" class="btn-ghost" :disabled="isAnimating" @click="resetAll">
            重置
          </button>
        </div>

        <!-- Switch behavior -->
        <div
          v-if="currentStep && currentStep.switchBehavior"
          class="rounded-xl border border-amber-200 bg-amber-50/80 p-4 shadow-sm"
        >
          <h3 class="text-sm font-semibold text-amber-900 flex items-center gap-2 mb-2">
            <Network class="w-4 h-4" />
            交换机 S 处理行为
            <span
              v-if="currentStep.switchAction && currentStep.switchAction !== 'none'"
              class="text-[10px] font-mono px-2 py-0.5 rounded-full border"
              :class="switchActionBadgeClass"
            >
              {{ switchActionLabel }}
            </span>
          </h3>
          <p class="text-xs text-amber-800 leading-relaxed">{{ currentStep.switchBehavior }}</p>
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
                <p class="text-base font-semibold text-emerald-900">{{ conclusion.title }}</p>
                <ul class="mt-2 space-y-1.5 text-sm text-emerald-800 list-disc list-inside">
                  <li v-for="(pt, i) in conclusion.points" :key="i">{{ pt }}</li>
                </ul>
              </div>
            </div>
          </div>
        </Transition>

        <!-- Tables -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div class="rounded-xl border border-slate-200 bg-white overflow-hidden shadow-sm">
            <div class="px-4 py-2.5 border-b border-slate-100 bg-slate-50">
              <h3 class="text-sm font-semibold text-slate-800">H1 ARP 表</h3>
            </div>
            <table class="w-full text-xs font-mono">
              <thead>
                <tr class="text-slate-500 border-b border-slate-100">
                  <th class="text-left py-2 px-3 font-medium">IP 地址</th>
                  <th class="text-left py-2 px-3 font-medium">MAC 地址</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!arpTable.length">
                  <td colspan="2" class="py-4 px-3 text-slate-400 text-center">（空）</td>
                </tr>
                <tr
                  v-for="(row, i) in arpTable"
                  :key="i"
                  class="border-b border-slate-50 last:border-0 transition-colors"
                  :class="rowJustAdded(i, 'arp') ? 'bg-emerald-50' : ''"
                >
                  <td class="py-2 px-3 text-slate-700">{{ row.ip }}</td>
                  <td class="py-2 px-3 text-emerald-700">{{ row.mac }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="rounded-xl border border-slate-200 bg-white overflow-hidden shadow-sm">
            <div class="px-4 py-2.5 border-b border-slate-100 bg-slate-50">
              <h3 class="text-sm font-semibold text-slate-800">交换机 S · MAC 地址表</h3>
            </div>
            <table class="w-full text-xs font-mono">
              <thead>
                <tr class="text-slate-500 border-b border-slate-100">
                  <th class="text-left py-2 px-3 font-medium">MAC</th>
                  <th class="text-left py-2 px-3 font-medium">端口</th>
                  <th class="text-left py-2 px-3 font-medium hidden sm:table-cell">备注</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="!switchTable.length">
                  <td colspan="3" class="py-4 px-3 text-slate-400 text-center">（空）</td>
                </tr>
                <tr
                  v-for="(row, i) in switchTable"
                  :key="i"
                  class="border-b border-slate-50 last:border-0 transition-colors"
                  :class="rowJustAdded(i, 'switch') ? 'bg-emerald-50' : ''"
                >
                  <td class="py-2 px-3 text-slate-700 truncate max-w-[120px]" :title="row.mac">
                    {{ row.mac }}
                  </td>
                  <td class="py-2 px-3 text-slate-700">{{ row.port }}</td>
                  <td class="py-2 px-3 text-slate-500 hidden sm:table-cell">{{ row.note }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <!-- Right panel -->
      <aside class="space-y-4 flex flex-col min-h-0">
        <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm shrink-0">
          <h2 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
            <FileCode class="w-4 h-4 text-emerald-600" />
            当前步骤详情
          </h2>
          <template v-if="currentStep">
            <dl class="grid grid-cols-[auto_1fr] gap-x-3 gap-y-2 text-xs">
              <dt class="text-slate-500">步骤</dt>
              <dd class="text-slate-800 font-medium">
                #{{ currentStep.stepNum }} — {{ currentStep.name }}
                <span
                  v-if="currentStep.isT0"
                  class="ml-1 text-[10px] px-1.5 py-0.5 rounded bg-teal-100 text-teal-700"
                  >t0</span
                >
                <span
                  v-if="currentStep.isT1"
                  class="ml-1 text-[10px] px-1.5 py-0.5 rounded bg-emerald-100 text-emerald-700"
                  >t1</span
                >
              </dd>
              <dt class="text-slate-500">起点</dt>
              <dd class="text-slate-700 font-mono">{{ currentStep.from }}</dd>
              <dt class="text-slate-500">终点</dt>
              <dd class="text-slate-700 font-mono">{{ currentStep.to }}</dd>
              <dt class="text-slate-500">协议</dt>
              <dd class="text-emerald-700 font-mono">{{ currentStep.protocol }}</dd>
              <dt class="text-slate-500">通信目的</dt>
              <dd class="text-slate-700 col-span-1 leading-relaxed">{{ currentStep.purpose }}</dd>
              <dt class="text-slate-500">传输方式</dt>
              <dd>
                <span
                  class="font-mono px-1.5 py-0.5 rounded text-[10px]"
                  :class="
                    currentStep.transmission === '广播'
                      ? 'bg-amber-100 text-amber-800'
                      : 'bg-slate-100 text-slate-700'
                  "
                >
                  {{ currentStep.transmission }}
                </span>
              </dd>
              <dt class="text-slate-500">源 MAC</dt>
              <dd class="text-slate-600 font-mono text-[10px] break-all">{{ currentStep.srcMac }}</dd>
              <dt class="text-slate-500">目的 MAC</dt>
              <dd class="text-slate-600 font-mono text-[10px] break-all">{{ currentStep.dstMac }}</dd>
              <dt class="text-slate-500">源 IP</dt>
              <dd class="text-slate-600 font-mono text-[10px]">{{ currentStep.srcIp }}</dd>
              <dt class="text-slate-500">目的 IP</dt>
              <dd class="text-slate-600 font-mono text-[10px]">{{ currentStep.dstIp }}</dd>
            </dl>
          </template>
          <p v-else class="text-xs text-slate-400 font-mono">点击「开始」后使用「下一步」推进</p>
        </div>

        <div
          class="rounded-2xl border border-slate-200 bg-white flex flex-col flex-1 min-h-[260px] overflow-hidden shadow-sm"
        >
          <h2 class="text-sm font-semibold text-slate-800 p-4 pb-2 flex items-center gap-2 shrink-0">
            <ListOrdered class="w-4 h-4 text-emerald-600" />
            步骤日志
          </h2>
          <ol
            ref="logContainer"
            class="flex-1 overflow-y-auto px-4 pb-4 space-y-2 text-xs scroll-smooth"
          >
            <li
              v-for="(entry, idx) in logEntries"
              :key="idx"
              class="flex gap-2 p-2 rounded-lg border transition-colors font-mono"
              :class="
                idx === logEntries.length - 1
                  ? 'border-emerald-300 bg-emerald-50'
                  : 'border-slate-100 bg-slate-50'
              "
            >
              <span class="text-slate-400 shrink-0 w-6">#{{ entry.stepNum }}</span>
              <div class="min-w-0 font-sans">
                <p class="text-slate-800 font-medium">{{ entry.stepName }}</p>
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
import { ref, computed, nextTick, watch, defineComponent, h } from 'vue'
import {
  Monitor,
  Server,
  Router,
  Globe,
  Cloud,
  Network,
  FileCode,
  ListOrdered,
  CheckCircle2,
} from 'lucide-vue-next'
import {
  DEVICES,
  SCENARIO_META,
  SCENARIO_STEPS,
  SCENARIO_CONCLUSION,
  INITIAL_ARP_TABLE,
  INITIAL_SWITCH_TABLE,
} from '../data/scenarioSteps.js'

const LINK_SEGMENT_MS = 550

const meta = SCENARIO_META
const conclusion = SCENARIO_CONCLUSION
const devices = DEVICES
const steps = SCENARIO_STEPS

const nodeIcons = {
  h1: Monitor,
  h2: Server,
  dns: Server,
  s: Network,
  r: Router,
  internet: Cloud,
  web: Globe,
}

const TopologyNode = defineComponent({
  name: 'TopologyNode',
  props: {
    device: { type: Object, required: true },
    icon: { type: Object, required: true },
    active: Boolean,
    processing: Boolean,
  },
  setup(props) {
    return () =>
      h(
        'article',
        {
          class: [
            'rounded-xl border-2 p-3 min-w-[100px] max-w-[140px] transition-all duration-500',
            props.active
              ? 'border-emerald-400 bg-emerald-50/70 shadow-md shadow-emerald-100'
              : props.processing
                ? 'border-amber-300 bg-amber-50/50'
                : 'border-slate-200 bg-white',
          ],
          'aria-label': props.device.label,
        },
        [
          h('div', { class: 'flex items-center gap-1.5 mb-1' }, [
            h(props.icon, { class: 'w-4 h-4 text-emerald-600 shrink-0' }),
            h('span', { class: 'font-semibold text-xs text-slate-800' }, props.device.label),
          ]),
          props.device.ip !== '—'
            ? h('p', { class: 'text-[10px] text-slate-600 font-mono' }, props.device.ip)
            : null,
          props.device.mac !== '—'
            ? h(
                'p',
                {
                  class: 'text-[9px] text-slate-400 font-mono truncate',
                  title: props.device.mac,
                },
                props.device.mac
              )
            : null,
        ]
      )
  },
})

const mode = ref('idle')
const currentStepIndex = ref(-1)
const isAnimating = ref(false)
const arpTable = ref([...INITIAL_ARP_TABLE])
const switchTable = ref([...INITIAL_SWITCH_TABLE])
const logEntries = ref([])
const logContainer = ref(null)

const activeLinkId = ref(null)
const activePacketLabel = ref(null)
const packetProgress = ref(0)
const animLinkIndex = ref(0)

const prevArpLen = ref(0)
const prevSwitchLen = ref(0)

let animTimer = null

const currentStep = computed(() =>
  currentStepIndex.value >= 0 ? steps[currentStepIndex.value] : null
)

const phaseLabel = computed(() => {
  const map = { idle: '空闲', running: '演示进行中', done: 't0→t1 完成' }
  return map[mode.value] ?? mode.value
})

const controlsDisabled = computed(() => isAnimating.value)
const canNext = computed(
  () =>
    !isAnimating.value &&
    mode.value === 'running' &&
    currentStepIndex.value < steps.length - 1
)
const canPrev = computed(
  () => !isAnimating.value && mode.value === 'running' && currentStepIndex.value >= 0
)
const stepDisplay = computed(() =>
  mode.value === 'running'
    ? `${Math.max(0, currentStepIndex.value + 1)}/${steps.length}`
    : '—'
)

const switchActionLabel = computed(() => {
  const map = { flood: '泛洪', forward: '转发', none: '—' }
  return map[currentStep.value?.switchAction] ?? '—'
})

const switchActionBadgeClass = computed(() => {
  const a = currentStep.value?.switchAction
  if (a === 'flood') return 'border-amber-300 bg-amber-100 text-amber-800'
  if (a === 'forward') return 'border-emerald-300 bg-emerald-100 text-emerald-800'
  return 'border-slate-200 bg-slate-100 text-slate-600'
})

/** 报文气泡沿拓扑纵向大致位置 */
const packetBubbleStyle = computed(() => {
  const positions = {
    'h1-s': { top: '18%', left: '50%' },
    'h2-s': { top: '18%', left: '50%' },
    'dns-s': { top: '18%', left: '50%' },
    's-r': { top: '42%', left: '50%' },
    'r-inet': { top: '58%', left: '50%' },
    'inet-web': { top: '74%', left: '50%' },
  }
  const base = positions[activeLinkId.value] ?? { top: '50%', left: '50%' }
  const offset = (packetProgress.value - 0.5) * 24
  return {
    ...base,
    transform: `translate(calc(-50% + ${offset}px), -50%)`,
  }
})

function isNodeActive(id) {
  if (!currentStep.value) return false
  return currentStep.value.pathNodes?.includes(id)
}

function isNodeProcessing(id) {
  if (isAnimating.value) return false
  if (!currentStep.value) return false
  const fromMap = { H1: 'h1', '本地 DNS': 'dns', R: 'r', 'Web Server': 'web' }
  const fromId = fromMap[currentStep.value.from]
  return fromId === id
}

function linkBarClass(linkId) {
  if (isAnimating.value && activeLinkId.value === linkId) return 'bg-emerald-500 w-0.5'
  if (currentStep.value?.pathLinks?.includes(linkId)) return 'bg-emerald-300 w-0.5'
  return 'bg-slate-200 w-px'
}

function rowJustAdded(index, table) {
  if (table === 'arp') return index >= prevArpLen.value
  return index >= prevSwitchLen.value
}

function applyStepState(step) {
  prevArpLen.value = arpTable.value.length
  prevSwitchLen.value = switchTable.value.length
  arpTable.value = step.arpTable.map((r) => ({ ...r }))
  switchTable.value = step.switchTable.map((r) => ({ ...r }))
}

function appendLog(step) {
  logEntries.value.push({
    stepNum: step.stepNum,
    stepName: step.name,
    summary: step.summary,
  })
  nextTick(() => {
    if (logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight
  })
}

function clearAnim() {
  if (animTimer) {
    clearTimeout(animTimer)
    animTimer = null
  }
  isAnimating.value = false
  activeLinkId.value = null
  activePacketLabel.value = null
  packetProgress.value = 0
  animLinkIndex.value = 0
}

function animateLinkSegment(linkId, reverse, onDone) {
  activeLinkId.value = linkId
  packetProgress.value = reverse ? 1 : 0
  const start = performance.now()

  function frame(now) {
    const t = Math.min(1, (now - start) / LINK_SEGMENT_MS)
    const eased = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
    packetProgress.value = reverse ? 1 - eased : eased
    if (t < 1) {
      requestAnimationFrame(frame)
    } else {
      onDone()
    }
  }
  requestAnimationFrame(frame)
}

function runPathAnimation(step, onComplete) {
  const links = step.pathLinks ?? []
  if (!step.animate || !links.length) {
    onComplete()
    return
  }

  isAnimating.value = true
  activePacketLabel.value = step.packetLabel
  animLinkIndex.value = 0
  const ordered = step.animateReverse ? [...links].reverse() : links

  function playNext() {
    if (animLinkIndex.value >= ordered.length) {
      clearAnim()
      onComplete()
      return
    }
    const linkId = ordered[animLinkIndex.value]
    animateLinkSegment(linkId, step.animateReverse, () => {
      animLinkIndex.value += 1
      playNext()
    })
  }
  playNext()
}

function executeStep(forward = true) {
  const nextIdx = forward ? currentStepIndex.value + 1 : currentStepIndex.value
  if (forward && nextIdx >= steps.length) return
  const step = steps[nextIdx]

  const finish = () => {
    applyStepState(step)
    if (forward) {
      appendLog(step)
      currentStepIndex.value = nextIdx
      if (nextIdx === steps.length - 1) mode.value = 'done'
    }
  }

  if (forward) {
    runPathAnimation(step, finish)
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
    stepName: '场景初始化',
    summary: `${meta.t0}；ARP 表与交换表均为空`,
  })
}

function nextStep() {
  if (mode.value === 'running' && canNext.value) executeStep(true)
}

function prevStep() {
  if (!canPrev.value) return
  if (currentStepIndex.value <= 0) {
    currentStepIndex.value = -1
    arpTable.value = []
    switchTable.value = []
    prevArpLen.value = 0
    prevSwitchLen.value = 0
    logEntries.value = logEntries.value.slice(0, 1)
    return
  }
  const newIdx = currentStepIndex.value - 1
  const step = steps[newIdx]
  applyStepState(step)
  currentStepIndex.value = newIdx
  mode.value = 'running'
  logEntries.value = logEntries.value.slice(0, newIdx + 2)
  prevArpLen.value = 0
  prevSwitchLen.value = 0
}

function resetAll(clearLogs = true) {
  clearAnim()
  mode.value = 'idle'
  currentStepIndex.value = -1
  arpTable.value = []
  switchTable.value = []
  prevArpLen.value = 0
  prevSwitchLen.value = 0
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
  @apply px-4 py-2 rounded-lg text-sm font-medium bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm
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
    transform: translate(-50%, -50%) scale(0.85);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}
.result-fade-enter-active {
  animation: result-slide-in 0.35s ease-out;
}
@keyframes result-slide-in {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
