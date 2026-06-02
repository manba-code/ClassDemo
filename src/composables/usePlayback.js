import { ref, computed } from 'vue'

/**
 * 协议过程回放状态（EXT-02）
 * @param {object} options
 * @param {() => object} options.captureSnapshot - 捕获当前状态快照
 * @param {(snapshot: object) => void} options.restoreSnapshot - 从快照恢复
 * @param {import('vue').Ref<number>} [options.baseAnimMsRef] - 基础动画时长 ref
 */
export function usePlayback({ captureSnapshot, restoreSnapshot, baseAnimMsRef }) {
  const isPaused = ref(false)
  const isPlaying = ref(false)
  const playbackSpeed = ref(1)
  const stepHistory = ref([])

  let pauseResolve = null
  let animPausedAt = null
  let animStartTime = null
  let animElapsedBeforePause = 0

  const speedOptions = [
    { value: 0.5, label: '0.5x' },
    { value: 1, label: '1x' },
    { value: 2, label: '2x' },
  ]

  const animDurationMs = computed(() => {
    const base = baseAnimMsRef?.value ?? 900
    return base / playbackSpeed.value
  })

  function pushHistory(index) {
    stepHistory.value.push({ index, snapshot: captureSnapshot() })
  }

  function clearHistory() {
    stepHistory.value = []
  }

  function restoreToIndex(index) {
    const entry = stepHistory.value.find((h) => h.index === index)
    if (entry) restoreSnapshot(entry.snapshot)
  }

  function restorePrevious(index) {
    const prev = stepHistory.value.filter((h) => h.index < index).pop()
    if (prev) restoreSnapshot(prev.snapshot)
  }

  /** 可暂停的动画包装 */
  function runPausableAnimation(durationMs, onFrame, onComplete) {
    isPlaying.value = true
    isPaused.value = false
    animStartTime = performance.now()
    animElapsedBeforePause = 0

    function frame(now) {
      if (isPaused.value) {
        pauseResolve = () => requestAnimationFrame(frame)
        return
      }

      const elapsed = animElapsedBeforePause + (now - animStartTime)
      const t = Math.min(1, elapsed / durationMs)
      onFrame(t)

      if (t < 1) {
        requestAnimationFrame(frame)
      } else {
        isPlaying.value = false
        onComplete?.()
      }
    }

    requestAnimationFrame(frame)
  }

  function togglePause() {
    if (!isPlaying.value) return

    if (isPaused.value) {
      isPaused.value = false
      animStartTime = performance.now()
      pauseResolve?.()
      pauseResolve = null
    } else {
      isPaused.value = true
      animElapsedBeforePause += performance.now() - animStartTime
    }
  }

  function cancelAnimation() {
    isPlaying.value = false
    isPaused.value = false
    pauseResolve = null
    animElapsedBeforePause = 0
  }

  function setSpeed(speed) {
    playbackSpeed.value = speed
  }

  return {
    isPaused,
    isPlaying,
    playbackSpeed,
    stepHistory,
    speedOptions,
    animDurationMs,
    pushHistory,
    clearHistory,
    restoreToIndex,
    restorePrevious,
    runPausableAnimation,
    togglePause,
    cancelAnimation,
    setSpeed,
  }
}
