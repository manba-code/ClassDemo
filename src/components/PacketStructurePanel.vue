<script setup>
import { ref, computed } from 'vue'
import { Layers, ChevronDown, ChevronRight } from 'lucide-vue-next'

const props = defineProps({
  /** @type {import('../utils/buildPacketLayers.js').PacketLayer[]} */
  layers: {
    type: Array,
    default: () => [],
  },
  title: {
    type: String,
    default: '报文结构解析',
  },
})

const expanded = ref(true)
const openLayers = ref({})

const hasLayers = computed(() => props.layers.length > 0)

function isLayerOpen(layerName) {
  return openLayers.value[layerName] !== false
}

function toggleLayer(layerName) {
  openLayers.value[layerName] = !isLayerOpen(layerName)
}
</script>

<template>
  <div class="rounded-2xl border border-slate-200 bg-white overflow-hidden shadow-sm">
    <button
      type="button"
      class="w-full flex items-center justify-between gap-2 px-4 py-3 text-left hover:bg-slate-50 transition-colors"
      @click="expanded = !expanded"
    >
      <span class="text-sm font-semibold text-slate-800 flex items-center gap-2">
        <Layers class="w-4 h-4 text-violet-600" />
        {{ title }}
      </span>
      <component :is="expanded ? ChevronDown : ChevronRight" class="w-4 h-4 text-slate-400" />
    </button>

    <div v-show="expanded" class="border-t border-slate-100 px-4 pb-4">
      <p v-if="!hasLayers" class="text-xs text-slate-400 py-3 font-mono">
        当前步骤无网络报文（如本地处理或空闲状态）
      </p>

      <div v-else class="space-y-2 pt-3">
        <div
          v-for="layer in layers"
          :key="layer.layer"
          class="rounded-lg border border-slate-100 overflow-hidden"
        >
          <button
            type="button"
            class="w-full flex items-center gap-2 px-3 py-2 bg-slate-50 hover:bg-slate-100 text-left transition-colors"
            @click="toggleLayer(layer.layer)"
          >
            <component
              :is="isLayerOpen(layer.layer) ? ChevronDown : ChevronRight"
              class="w-3.5 h-3.5 text-slate-400 shrink-0"
            />
            <span class="text-xs font-semibold text-violet-800">{{ layer.layer }}</span>
          </button>

          <ul v-show="isLayerOpen(layer.layer)" class="divide-y divide-slate-50">
            <li
              v-for="field in layer.fields"
              :key="field.name"
              class="grid grid-cols-[100px_1fr] gap-x-2 gap-y-0.5 px-3 py-2 text-xs"
              :class="field.changed ? 'bg-amber-50/80' : ''"
            >
              <span class="text-slate-500 font-medium">{{ field.name }}</span>
              <span
                class="font-mono break-all"
                :class="field.changed ? 'text-amber-800 font-semibold' : 'text-slate-800'"
              >
                {{ field.value }}
              </span>
              <span class="col-span-2 text-[10px] text-slate-400 leading-relaxed">{{ field.desc }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
