<script setup>
import { ref, computed, nextTick, watch, defineComponent, h, onMounted, onUnmounted } from 'vue'
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
  AlertTriangle,
  ChevronDown,
  ChevronRight,
} from 'lucide-vue-next'
import { SCENARIO_META, DEVICES } from '../data/scenarioSteps.js'
import {
  RUN_MODES,
  getStepsForMode,
  getConclusionForMode,
  getFaultProfile,
} from '../data/faultProfiles.js'
import { buildScenarioPacketLayers } from '../utils/buildPacketLayers.js'
import { usePlayback } from '../composables/usePlayback.js'
import PlaybackControls from './PlaybackControls.vue'
import PacketStructurePanel from './PacketStructurePanel.vue'

const LINK_SEGMENT_MS_BASE = ref(550)

const meta = SCENARIO_META
const devices = DEVICES

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
    fault: Boolean,
  },
  setup(props) {
    return () =>
      h(
        'article',
        {
          class: [
            'rounded-xl border-2 p-3 min-w-[100px] max-w-[140px] transition-all duration-500',
            props.fault
              ? 'border-red-400 bg-red-50/70 shadow-md shadow-red-100'
              : props.active
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
                { class: 'text-[9px] text-slate-400 font-mono truncate', title: props.device.mac },
                props.device.mac
              )
            : null,
        ]
      )
  },
})

const runMode = ref('normal')
const mode = ref('idle')
const currentStepIndex = ref(-1)
const isAnimating = ref(false)
const arpTable = ref([])
const switchTable = ref([])
const logEntries = ref([])
const logContainer = ref(null)
const showCompare = ref(false)

const activeLinkId = ref(null)
const activePacketLabel = ref(null)
const packetProgress = ref(0)
const animLinkIndex = ref(0)
const prevStepSnapshot = ref(null)

const prevArpLen = ref(0)
const prevSwitchLen = ref(0)

let animCancel = null

const steps = computed(() => getStepsForMode(runMode.value))
const conclusion = computed(() => getConclusionForMode(runMode.value))
const faultProfile = computed(() => getFaultProfile(runMode.value))
const isFaultMode = computed(() => runMode.value !== 'normal')

function captureSnapshot() {
  return {
    currentStepIndex: currentStepIndex.value,
    mode: mode.value,
    arpTable: arpTable.value.map((r) => ({ ...r })),
    switchTable: switchTable.value.map((r) => ({ ...r })),
    logEntries: logEntries.value.map((e) => ({ ...e })),
    prevArpLen: prevArpLen.value,
    prevSwitchLen: prevSwitchLen.value,
    prevStepSnapshot: prevStepSnapshot.value ? { ...prevStepSnapshot.value } : null,
  }
}

function restoreSnapshot(snap) {
  currentStepIndex.value = snap.currentStepIndex
  mode.value = snap.mode
  arpTable.value = snap.arpTable.map((r) => ({ ...r }))
  switchTable.value = snap.switchTable.map((r) => ({ ...r }))
  logEntries.value = snap.logEntries.map((e) => ({ ...e }))
  prevArpLen.value = snap.prevArpLen
  prevSwitchLen.value = snap.prevSwitchLen
  prevStepSnapshot.value = snap.prevStepSnapshot ? { ...snap.prevStepSnapshot } : null
}

const playback = usePlayback({
  captureSnapshot,
  restoreSnapshot,
  baseAnimMsRef: LINK_SEGMENT_MS_BASE,
})

const currentStep = computed(() =>
  currentStepIndex.value >= 0 ? steps.value[currentStepIndex.value] : null
)

const packetLayers = computed(() => {
  if (!currentStep.value) return []
  return buildScenarioPacketLayers(currentStep.value, prevStepSnapshot.value)
})

const phaseLabel = computed(() => {
  if (isFaultMode.value && mode.value === 'done') return '故障演示完成'
  const map = { idle: '空闲', running: '演示进行中', done: 't0→t1 完成' }
  return map[mode.value] ?? mode.value
})

const modeLocked = computed(() => mode.value !== 'idle')
const controlsDisabled = computed(() => isAnimating.value || playback.isPlaying.value)
const canNext = computed(
  () =>
    !controlsDisabled.value &&
    mode.value === 'running' &&
    currentStepIndex.value < steps.value.length - 1
)
const canPrev = computed(
  () => !controlsDisabled.value && mode.value === 'running' && currentStepIndex.value >= 0
)
const stepDisplay = computed(() =>
  mode.value === 'running'
    ? `${Math.max(0, currentStepIndex.value + 1)}/${steps.value.length}`
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
  return { ...base, transform: `translate(calc(-50% + ${offset}px), -50%)` }
})

const packetBubbleClass = computed(() => {
  if (currentStep.value?.isFault || currentStep.value?.isFaultPath) {
    return 'bg-red-50 border-red-400 text-red-800'
  }
  return 'bg-emerald-50 border-emerald-400 text-emerald-800'
})

function isNodeActive(id) {
  if (!currentStep.value) return false
  return currentStep.value.pathNodes?.includes(id)
}

function isNodeFault(id) {
  if (!currentStep.value?.isFault && !currentStep.value?.isFaultPath) return false
  return currentStep.value.pathNodes?.includes(id)
}

function isNodeProcessing(id) {
  if (isAnimating.value) return false
  if (!currentStep.value) return false
  const fromMap = { H1: 'h1', '本地 DNS': 'dns', R: 'r', 'Web Server': 'web' }
  return fromMap[currentStep.value.from] === id
}

function linkBarClass(linkId) {
  const isFaultLink =
    currentStep.value?.isFault || currentStep.value?.isFaultPath || currentStep.value?.isFaultConclusion
  if (isAnimating.value && activeLinkId.value === linkId) {
    return isFaultLink ? 'bg-red-500 w-0.5 border-dashed' : 'bg-emerald-500 w-0.5'
  }
  if (currentStep.value?.pathLinks?.includes(linkId)) {
    return isFaultLink ? 'bg-red-400 w-0.5 opacity-80' : 'bg-emerald-300 w-0.5'
  }
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
    faultCode: step.faultCode,
    isFault: step.isFault || step.isFaultConclusion,
  })
  nextTick(() => {
    if (logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight
  })
}

function clearAnim() {
  animCancel?.()
  animCancel = null
  playback.cancelAnimation()
  isAnimating.value = false
  activeLinkId.value = null
  activePacketLabel.value = null
  packetProgress.value = 0
  animLinkIndex.value = 0
}

function animateLinkSegment(linkId, reverse, onDone) {
  activeLinkId.value = linkId
  packetProgress.value = reverse ? 1 : 0
  const duration = LINK_SEGMENT_MS_BASE.value / playback.playbackSpeed.value

  playback.runPausableAnimation(
    duration,
    (t) => {
      const eased = t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2
      packetProgress.value = reverse ? 1 - eased : eased
    },
    onDone
  )
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
  if (forward && nextIdx >= steps.value.length) return
  const step = steps.value[nextIdx]

  if (forward) playback.pushHistory(currentStepIndex.value)

  const finish = () => {
    prevStepSnapshot.value = currentStep.value ? { ...currentStep.value } : null
    applyStepState(step)
    if (forward) {
      appendLog(step)
      currentStepIndex.value = nextIdx
      if (nextIdx === steps.value.length - 1) mode.value = 'done'
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
  playback.clearHistory()
  mode.value = 'running'
  currentStepIndex.value = -1
  prevStepSnapshot.value = null
  logEntries.value.push({
    stepNum: 0,
    stepName: '场景初始化',
    summary: isFaultMode.value
      ? `${meta.t0}；运行模式：${RUN_MODES.find((m) => m.value === runMode.value)?.label}`
      : `${meta.t0}；ARP 表与交换表均为空`,
  })
  playback.pushHistory(-1)
}

function nextStep() {
  if (mode.value === 'running' && canNext.value) executeStep(true)
}

function prevStep() {
  if (!canPrev.value) return
  if (currentStepIndex.value <= 0) {
    playback.restorePrevious(0)
    currentStepIndex.value = -1
    arpTable.value = []
    switchTable.value = []
    prevArpLen.value = 0
    prevSwitchLen.value = 0
    prevStepSnapshot.value = null
    logEntries.value = logEntries.value.slice(0, 1)
    return
  }
  const newIdx = currentStepIndex.value - 1
  playback.restoreToIndex(newIdx - 1)
  currentStepIndex.value = newIdx
  mode.value = 'running'
  logEntries.value = logEntries.value.slice(0, newIdx + 2)
}

function replayFromStart() {
  const savedMode = runMode.value
  resetAll(true)
  runMode.value = savedMode
  start()
}

function resetAll(clearLogs = true) {
  clearAnim()
  mode.value = 'idle'
  currentStepIndex.value = -1
  arpTable.value = []
  switchTable.value = []
  prevArpLen.value = 0
  prevSwitchLen.value = 0
  prevStepSnapshot.value = null
  playback.clearHistory()
  if (clearLogs) logEntries.value = []
}

function jumpToStep(idx) {
  if (controlsDisabled.value || mode.value !== 'running') return
  if (idx < 0 || idx > currentStepIndex.value) return
  playback.restoreToIndex(idx - 1)
  currentStepIndex.value = idx - 1
  logEntries.value = logEntries.value.slice(0, idx + 2)
  nextStep()
}

function onKeydown(e) {
  if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return
  if (e.code === 'Space') {
    e.preventDefault()
    playback.togglePause()
  } else if (e.code === 'ArrowRight' && canNext.value) {
    nextStep()
  } else if (e.code === 'ArrowLeft' && canPrev.value) {
    prevStep()
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))

watch(logEntries, () => {
  nextTick(() => {
    if (logContainer.value) logContainer.value.scrollTop = logContainer.value.scrollHeight
  })
})
</script>

<template>
  <div class="text-slate-800 font-sans selection:bg-emerald-200/60 pb-8">
    <header class="border-b border-slate-200 bg-white/90 backdrop-blur-sm px-6 py-4 shadow-sm">
      <div class="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 class="text-xl font-semibold tracking-tight text-slate-900">综合网络场景模拟</h1>
          <p class="text-sm text-slate-500 mt-0.5">H1 访问 {{ meta.domain }} · t0 → t1 分步演示</p>
        </div>
        <div
          class="flex items-center gap-2 text-xs font-mono px-3 py-1.5 rounded-full border border-slate-200 bg-white text-slate-600 shadow-sm"
        >
          <span
            class="w-2 h-2 rounded-full transition-colors duration-300"
            :class="
              isAnimating
                ? 'bg-amber-500 animate-pulse'
                : mode === 'done' && isFaultMode
                  ? 'bg-red-500'
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
        <!-- Run mode selector -->
        <div class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
          <p class="text-xs font-medium text-slate-500 mb-2">运行模式</p>
          <div class="flex flex-wrap gap-2">
            <label
              v-for="m in RUN_MODES"
              :key="m.value"
              class="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg border text-sm cursor-pointer transition-colors"
              :class="
                runMode === m.value
                  ? m.value === 'normal'
                    ? 'border-emerald-300 bg-emerald-50 text-emerald-800'
                    : 'border-red-300 bg-red-50 text-red-800'
                  : 'border-slate-200 text-slate-600 hover:bg-slate-50'
              "
            >
              <input
                v-model="runMode"
                type="radio"
                :value="m.value"
                class="sr-only"
                :disabled="modeLocked"
              />
              {{ m.label }}
            </label>
          </div>
          <p v-if="modeLocked" class="text-[10px] text-slate-400 mt-2">运行中不可切换模式，请重置后更改</p>
        </div>

        <!-- Topology -->
        <div class="relative rounded-2xl border border-slate-200 bg-white p-4 md:p-6 overflow-hidden shadow-sm">
          <div
            class="absolute inset-0 opacity-40 pointer-events-none"
            style="
              background-image: radial-gradient(circle at 1px 1px, #cbd5e1 1px, transparent 0);
              background-size: 24px 24px;
            "
          />
          <p class="relative text-[10px] font-mono text-slate-400 text-center mb-3">局域网 192.168.1.0/24</p>

          <div class="relative flex flex-col items-center gap-2 min-h-[320px]">
            <div class="flex flex-wrap justify-center gap-3 md:gap-6 w-full">
              <TopologyNode
                v-for="id in ['h1', 'h2', 'dns']"
                :key="id"
                :device="devices[id]"
                :icon="nodeIcons[id]"
                :active="isNodeActive(id)"
                :processing="isNodeProcessing(id)"
                :fault="isNodeFault(id)"
              />
            </div>
            <div class="flex justify-center gap-16 md:gap-24 h-6 w-full max-w-md">
              <div
                v-for="lid in ['h1-s', 'h2-s', 'dns-s']"
                :key="lid"
                class="w-px flex-1 max-w-[2px] self-stretch rounded-full transition-colors duration-300"
                :class="linkBarClass(lid)"
              />
            </div>
            <TopologyNode
              :device="devices.s"
              :icon="Network"
              :active="isNodeActive('s')"
              :processing="isNodeProcessing('s')"
              :fault="isNodeFault('s')"
              class="z-10"
            />
            <div class="w-px h-6 rounded-full transition-colors duration-300" :class="linkBarClass('s-r')" />
            <TopologyNode
              :device="devices.r"
              :icon="Router"
              :active="isNodeActive('r')"
              :processing="isNodeProcessing('r')"
              :fault="isNodeFault('r')"
            />
            <div class="w-px h-6 rounded-full transition-colors duration-300" :class="linkBarClass('r-inet')" />
            <TopologyNode
              :device="devices.internet"
              :icon="Cloud"
              :active="isNodeActive('internet')"
              :processing="isNodeProcessing('internet')"
              :fault="isNodeFault('internet')"
            />
            <div class="w-px h-6 rounded-full transition-colors duration-300" :class="linkBarClass('inet-web')" />
            <TopologyNode
              :device="devices.web"
              :icon="Globe"
              :active="isNodeActive('web')"
              :processing="isNodeProcessing('web')"
              :fault="isNodeFault('web')"
            />
            <Transition name="packet-pop">
              <div
                v-if="isAnimating && activePacketLabel"
                class="absolute z-30 pointer-events-none"
                :style="packetBubbleStyle"
              >
                <div
                  class="px-2.5 py-1 rounded-md text-[10px] font-mono font-bold shadow-lg border whitespace-nowrap"
                  :class="packetBubbleClass"
                >
                  {{ activePacketLabel }}
                </div>
              </div>
            </Transition>
          </div>
        </div>

        <!-- Controls -->
        <PlaybackControls
          :can-prev="canPrev"
          :can-next="canNext"
          :can-pause="isAnimating"
          :is-paused="playback.isPaused.value"
          :is-playing="playback.isPlaying.value"
          :can-replay="mode !== 'idle'"
          :can-reset="true"
          :step-display="stepDisplay"
          :playback-speed="playback.playbackSpeed.value"
          :speed-options="playback.speedOptions"
          :extra-disabled="false"
          @prev="prevStep"
          @next="nextStep"
          @pause="playback.togglePause()"
          @replay="replayFromStart"
          @reset="resetAll()"
          @speed="playback.setSpeed"
        >
          <template #leading>
            <button
              type="button"
              class="btn-primary"
              :disabled="controlsDisabled || mode === 'running'"
              @click="start"
            >
              开始
            </button>
          </template>
        </PlaybackControls>

        <!-- Fault compare panel -->
        <div
          v-if="isFaultMode && faultProfile"
          class="rounded-xl border border-slate-200 bg-white overflow-hidden shadow-sm"
        >
          <button
            type="button"
            class="w-full flex items-center justify-between px-4 py-3 text-left hover:bg-slate-50"
            @click="showCompare = !showCompare"
          >
            <span class="text-sm font-medium text-slate-700">与正常模式对比</span>
            <component :is="showCompare ? ChevronDown : ChevronRight" class="w-4 h-4 text-slate-400" />
          </button>
          <p v-show="showCompare" class="px-4 pb-3 text-xs text-slate-600 leading-relaxed">
            {{ faultProfile.normalCompare }}
          </p>
        </div>

        <!-- Switch behavior -->
        <div
          v-if="currentStep && currentStep.switchBehavior"
          class="rounded-xl border p-4 shadow-sm"
          :class="
            currentStep.isFault || currentStep.isFaultConclusion
              ? 'border-red-200 bg-red-50/80'
              : 'border-amber-200 bg-amber-50/80'
          "
        >
          <h3 class="text-sm font-semibold flex items-center gap-2 mb-2" :class="currentStep.isFault ? 'text-red-900' : 'text-amber-900'">
            <Network class="w-4 h-4" />
            交换机 S 处理行为
          </h3>
          <p class="text-xs leading-relaxed" :class="currentStep.isFault ? 'text-red-800' : 'text-amber-800'">
            {{ currentStep.switchBehavior }}
          </p>
          <p v-if="currentStep.faultCode" class="mt-2 text-xs font-mono text-red-700">
            故障码：{{ currentStep.faultCode }}
            <span v-if="currentStep.faultReason"> — {{ currentStep.faultReason }}</span>
          </p>
        </div>

        <!-- Result -->
        <Transition name="result-fade">
          <div
            v-if="mode === 'done'"
            class="rounded-xl border-2 p-5 shadow-sm"
            :class="conclusion.isFault ? 'border-red-300 bg-red-50' : 'border-emerald-300 bg-emerald-50'"
            role="status"
          >
            <div class="flex items-start gap-3">
              <component
                :is="conclusion.isFault ? AlertTriangle : CheckCircle2"
                class="w-6 h-6 shrink-0"
                :class="conclusion.isFault ? 'text-red-600' : 'text-emerald-600'"
              />
              <div>
                <p class="text-base font-semibold" :class="conclusion.isFault ? 'text-red-900' : 'text-emerald-900'">
                  {{ conclusion.title }}
                </p>
                <ul
                  class="mt-2 space-y-1.5 text-sm list-disc list-inside"
                  :class="conclusion.isFault ? 'text-red-800' : 'text-emerald-800'"
                >
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
                </tr>
              </thead>
              <tbody>
                <tr v-if="!switchTable.length">
                  <td colspan="2" class="py-4 px-3 text-slate-400 text-center">（空）</td>
                </tr>
                <tr
                  v-for="(row, i) in switchTable"
                  :key="i"
                  class="border-b border-slate-50 last:border-0 transition-colors"
                  :class="rowJustAdded(i, 'switch') ? 'bg-emerald-50' : ''"
                >
                  <td class="py-2 px-3 text-slate-700 truncate max-w-[120px]">{{ row.mac }}</td>
                  <td class="py-2 px-3 text-slate-700">{{ row.port }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <aside class="space-y-4 flex flex-col min-h-0">
        <div class="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm shrink-0">
          <h2 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-2">
            <FileCode class="w-4 h-4 text-emerald-600" />
            当前步骤详情
          </h2>
          <template v-if="currentStep">
            <dl class="grid grid-cols-[auto_1fr] gap-x-3 gap-y-2 text-xs">
              <dt class="text-slate-500">步骤</dt>
              <dd class="text-slate-800 font-medium">#{{ currentStep.stepNum }} — {{ currentStep.name }}</dd>
              <dt class="text-slate-500">协议</dt>
              <dd class="text-emerald-700 font-mono">{{ currentStep.protocol }}</dd>
              <dt class="text-slate-500">通信目的</dt>
              <dd class="text-slate-700 leading-relaxed">{{ currentStep.purpose }}</dd>
              <dt v-if="currentStep.faultCode" class="text-slate-500">故障码</dt>
              <dd v-if="currentStep.faultCode" class="text-red-700 font-mono">{{ currentStep.faultCode }}</dd>
            </dl>
          </template>
          <p v-else class="text-xs text-slate-400 font-mono">点击「开始」后使用「下一步」推进</p>
        </div>

        <PacketStructurePanel :layers="packetLayers" />

        <div class="rounded-2xl border border-slate-200 bg-white flex flex-col flex-1 min-h-[200px] overflow-hidden shadow-sm">
          <h2 class="text-sm font-semibold text-slate-800 p-4 pb-2 flex items-center gap-2 shrink-0">
            <ListOrdered class="w-4 h-4 text-emerald-600" />
            步骤日志
          </h2>
          <ol ref="logContainer" class="flex-1 overflow-y-auto px-4 pb-4 space-y-2 text-xs scroll-smooth">
            <li
              v-for="(entry, idx) in logEntries"
              :key="idx"
              class="flex gap-2 p-2 rounded-lg border transition-colors font-mono cursor-pointer"
              :class="[
                idx === logEntries.length - 1
                  ? entry.isFault
                    ? 'border-red-300 bg-red-50'
                    : 'border-emerald-300 bg-emerald-50'
                  : 'border-slate-100 bg-slate-50 hover:bg-slate-100',
              ]"
              @click="idx > 0 && idx <= currentStepIndex + 1 ? jumpToStep(idx - 1) : null"
            >
              <span class="text-slate-400 shrink-0 w-6">#{{ entry.stepNum }}</span>
              <div class="min-w-0 font-sans">
                <p class="text-slate-800 font-medium">{{ entry.stepName }}</p>
                <p class="text-slate-500 mt-0.5">{{ entry.summary }}</p>
                <p v-if="entry.faultCode" class="text-red-600 text-[10px] mt-0.5">[{{ entry.faultCode }}]</p>
              </div>
            </li>
          </ol>
        </div>
      </aside>
    </main>
  </div>
</template>

<style scoped>
.btn-primary {
  @apply px-4 py-2 rounded-lg text-sm font-medium bg-emerald-600 hover:bg-emerald-500 text-white shadow-sm
    disabled:opacity-40 disabled:cursor-not-allowed transition-colors duration-150;
}
.packet-pop-enter-active {
  animation: packet-pop-in 0.2s ease-out;
}
@keyframes packet-pop-in {
  from { opacity: 0; transform: translate(-50%, -50%) scale(0.85); }
  to { opacity: 1; transform: translate(-50%, -50%) scale(1); }
}
.result-fade-enter-active {
  animation: result-slide-in 0.35s ease-out;
}
@keyframes result-slide-in {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
