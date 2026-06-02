<script setup>
import { SkipBack, SkipForward, Pause, Play, RotateCcw, RefreshCw } from 'lucide-vue-next'

defineProps({
  canPrev: { type: Boolean, default: false },
  canNext: { type: Boolean, default: false },
  canPause: { type: Boolean, default: false },
  isPaused: { type: Boolean, default: false },
  isPlaying: { type: Boolean, default: false },
  canReplay: { type: Boolean, default: false },
  canReset: { type: Boolean, default: true },
  stepDisplay: { type: String, default: '—' },
  showSpeed: { type: Boolean, default: true },
  playbackSpeed: { type: Number, default: 1 },
  speedOptions: {
    type: Array,
    default: () => [
      { value: 0.5, label: '0.5x' },
      { value: 1, label: '1x' },
      { value: 2, label: '2x' },
    ],
  },
  extraDisabled: { type: Boolean, default: false },
})

defineEmits(['prev', 'next', 'pause', 'replay', 'reset', 'speed'])
</script>

<template>
  <div
    class="flex flex-wrap items-center gap-2 p-4 rounded-xl border border-slate-200 bg-white shadow-sm"
    role="toolbar"
    aria-label="回放控制"
  >
    <slot name="leading" />

    <button
      type="button"
      class="btn-secondary inline-flex items-center gap-1.5"
      :disabled="!canPrev || extraDisabled || isPlaying"
      @click="$emit('prev')"
    >
      <SkipBack class="w-3.5 h-3.5" />
      上一步
    </button>

    <button
      type="button"
      class="btn-secondary inline-flex items-center gap-1.5"
      :disabled="!canNext || extraDisabled || isPlaying"
      @click="$emit('next')"
    >
      下一步
      <SkipForward class="w-3.5 h-3.5" />
      <span class="text-slate-500 ml-0.5">({{ stepDisplay }})</span>
    </button>

    <button
      v-if="canPause"
      type="button"
      class="btn-secondary inline-flex items-center gap-1.5"
      :disabled="extraDisabled || !isPlaying"
      @click="$emit('pause')"
    >
      <component :is="isPaused ? Play : Pause" class="w-3.5 h-3.5" />
      {{ isPaused ? '继续' : '暂停' }}
    </button>

    <button
      type="button"
      class="btn-secondary inline-flex items-center gap-1.5"
      :disabled="!canReplay || extraDisabled || isPlaying"
      @click="$emit('replay')"
    >
      <RotateCcw class="w-3.5 h-3.5" />
      从头回放
    </button>

    <button
      type="button"
      class="btn-ghost inline-flex items-center gap-1.5"
      :disabled="!canReset || extraDisabled || isPlaying"
      @click="$emit('reset')"
    >
      <RefreshCw class="w-3.5 h-3.5" />
      重置
    </button>

    <div v-if="showSpeed" class="flex items-center gap-1 ml-auto">
      <span class="text-[10px] text-slate-400 font-mono mr-1">速度</span>
      <button
        v-for="opt in speedOptions"
        :key="opt.value"
        type="button"
        class="px-2 py-1 rounded text-[10px] font-mono border transition-colors"
        :class="
          playbackSpeed === opt.value
            ? 'border-violet-300 bg-violet-50 text-violet-800'
            : 'border-slate-200 text-slate-500 hover:bg-slate-50'
        "
        :disabled="extraDisabled || isPlaying"
        @click="$emit('speed', opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>

    <slot name="trailing" />
  </div>
</template>

<style scoped>
.btn-secondary {
  @apply px-3 py-2 rounded-lg text-sm font-medium bg-white hover:bg-slate-50
    border border-slate-300 text-slate-700 disabled:opacity-40 disabled:cursor-not-allowed
    transition-colors duration-150 shadow-sm;
}
.btn-ghost {
  @apply px-3 py-2 rounded-lg text-sm font-medium text-slate-500 hover:text-slate-800
    hover:bg-slate-100 disabled:opacity-40 disabled:cursor-not-allowed transition-colors duration-150;
}
</style>
