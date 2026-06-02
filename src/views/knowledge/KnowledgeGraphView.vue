<script setup>
import { ref, onMounted, onUnmounted, shallowRef } from 'vue'
import * as echarts from 'echarts/core'
import { TreeChart } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getKnowledgeGraph } from '../../api/knowledgeGraph'
import {
  Loader2,
  AlertCircle,
  GitGraph,
  RefreshCw,
  BookOpen,
  Layers,
  Network,
} from 'lucide-vue-next'

echarts.use([TreeChart, TooltipComponent, CanvasRenderer])

const LAYER_COLORS = {
  application: '#8b5cf6',
  transport: '#3b82f6',
  network: '#06b6d4',
  datalink: '#10b981',
  physical: '#f59e0b',
}

const chartContainer = ref(null)
const chartInstance = shallowRef(null)
const loading = ref(true)
const error = ref('')
const graphData = ref(null)
const selectedNode = ref(null)

function transformGraphData(data) {
  return {
    name: data.name,
    nodeType: 'root',
    itemStyle: { color: '#6366f1' },
    children: data.children.map((layer) => ({
      name: layer.name,
      nodeType: 'layer',
      layerKey: layer.layerKey,
      itemStyle: { color: LAYER_COLORS[layer.layerKey] || '#6366f1' },
      children: (layer.children || []).map((kp) => ({
        name: kp.name,
        nodeType: 'knowledge',
        id: kp.id,
        layerKey: kp.layerKey,
        content: kp.content,
        itemStyle: { color: LAYER_COLORS[kp.layerKey] || '#64748b' },
      })),
    })),
  }
}

function buildChartOption(treeData) {
  return {
    tooltip: {
      trigger: 'item',
      triggerOn: 'mousemove',
      formatter(params) {
        const d = params.data
        if (d.nodeType === 'knowledge' && d.content) {
          const preview = d.content.length > 80 ? `${d.content.slice(0, 80)}…` : d.content
          return `<strong>${d.name}</strong><br/><span style="color:#64748b;font-size:12px">${preview}</span>`
        }
        return d.name
      },
    },
    series: [
      {
        type: 'tree',
        data: [treeData],
        top: '4%',
        left: '8%',
        bottom: '4%',
        right: '22%',
        symbol: 'circle',
        symbolSize: 10,
        orient: 'LR',
        expandAndCollapse: true,
        initialTreeDepth: 2,
        label: {
          position: 'left',
          verticalAlign: 'middle',
          align: 'right',
          fontSize: 12,
          color: '#334155',
        },
        leaves: {
          label: {
            position: 'right',
            verticalAlign: 'middle',
            align: 'left',
          },
        },
        emphasis: {
          focus: 'descendant',
          itemStyle: {
            borderColor: '#6366f1',
            borderWidth: 2,
          },
        },
        lineStyle: {
          color: '#cbd5e1',
          width: 1.5,
          curveness: 0.4,
        },
        animationDuration: 400,
        animationDurationUpdate: 300,
      },
    ],
  }
}

function handleNodeClick(params) {
  if (!params?.data) return
  const { nodeType, name, content, layerKey, id } = params.data
  selectedNode.value = { nodeType, name, content, layerKey, id }
}

function renderChart(treeData) {
  if (!chartContainer.value) return
  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartContainer.value)
    chartInstance.value.on('click', handleNodeClick)
  }
  chartInstance.value.setOption(buildChartOption(treeData), true)
}

function handleResize() {
  chartInstance.value?.resize()
}

async function loadGraph() {
  loading.value = true
  error.value = ''
  selectedNode.value = null
  try {
    const data = await getKnowledgeGraph()
    graphData.value = data
    const treeData = transformGraphData(data)
    renderChart(treeData)
  } catch (err) {
    graphData.value = null
    error.value = err.message || '加载知识图谱失败'
    chartInstance.value?.clear()
  } finally {
    loading.value = false
  }
}

const layerLabel = {
  application: '应用层',
  transport: '传输层',
  network: '网络层',
  datalink: '数据链路层',
  physical: '物理层',
}

onMounted(() => {
  loadGraph()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance.value?.dispose()
  chartInstance.value = null
})
</script>

<template>
  <div class="space-y-4">
    <section class="rounded-2xl border border-slate-200 bg-white p-4 md:p-6 shadow-sm">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
        <div class="flex items-center gap-2">
          <GitGraph class="w-5 h-5 text-violet-600 shrink-0" />
          <div>
            <h2 class="text-base font-semibold text-slate-900">知识图谱</h2>
            <p class="text-xs text-slate-500 mt-0.5">
              计算机网络 → 五层模型 → 各层知识点（点击节点查看详情）
            </p>
          </div>
        </div>
        <button
          type="button"
          class="inline-flex items-center gap-1.5 self-start px-3 py-1.5 text-sm font-medium rounded-lg
            border border-slate-200 text-slate-700 hover:bg-slate-50 transition-colors disabled:opacity-50"
          :disabled="loading"
          @click="loadGraph"
        >
          <RefreshCw class="w-3.5 h-3.5" :class="{ 'animate-spin': loading }" />
          刷新
        </button>
      </div>

      <div
        class="flex flex-wrap gap-3 mb-4 text-xs text-slate-600"
        aria-label="图例"
      >
        <span class="inline-flex items-center gap-1.5">
          <span class="w-2.5 h-2.5 rounded-full bg-indigo-500" />
          根节点
        </span>
        <span
          v-for="(color, key) in LAYER_COLORS"
          :key="key"
          class="inline-flex items-center gap-1.5"
        >
          <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: color }" />
          {{ layerLabel[key] }}
        </span>
      </div>

      <div v-if="loading" class="flex items-center justify-center gap-2 py-24 text-sm text-slate-500">
        <Loader2 class="w-5 h-5 animate-spin" />
        加载知识图谱…
      </div>

      <div
        v-else-if="error"
        class="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
      >
        <AlertCircle class="w-4 h-4 shrink-0 mt-0.5" />
        <div>
          <p>{{ error }}</p>
          <p class="mt-1 text-xs text-red-600/80">请确认后端已启动（8080）且 MySQL 可用</p>
        </div>
      </div>

      <div
        v-else
        class="grid gap-4 lg:grid-cols-[1fr_300px]"
      >
        <div
          ref="chartContainer"
          class="min-h-[480px] w-full rounded-xl border border-slate-100 bg-slate-50/30"
          role="img"
          aria-label="知识图谱树形结构"
        />

        <aside class="rounded-xl border border-slate-200 bg-slate-50/50 p-4 flex flex-col min-h-[200px] lg:min-h-[480px]">
          <h3 class="text-sm font-semibold text-slate-800 mb-3 flex items-center gap-1.5">
            <BookOpen class="w-4 h-4 text-violet-600" />
            节点详情
          </h3>

          <div
            v-if="!selectedNode"
            class="flex-1 flex flex-col items-center justify-center text-center text-sm text-slate-400 px-2"
          >
            <Network class="w-8 h-8 mb-2 opacity-40" />
            <p>点击树形图中的节点</p>
            <p class="text-xs mt-1">知识点节点将展示标题与详细内容</p>
          </div>

          <div v-else-if="selectedNode.nodeType === 'knowledge'" class="flex-1 overflow-y-auto">
            <span
              class="inline-block mb-2 rounded-md px-2 py-0.5 text-xs font-medium text-white"
              :style="{ backgroundColor: LAYER_COLORS[selectedNode.layerKey] || '#64748b' }"
            >
              {{ layerLabel[selectedNode.layerKey] || selectedNode.layerKey }}
            </span>
            <h4 class="text-base font-semibold text-slate-900 leading-snug">
              {{ selectedNode.name }}
            </h4>
            <p class="mt-3 text-sm text-slate-700 leading-relaxed whitespace-pre-wrap">
              {{ selectedNode.content || '暂无内容' }}
            </p>
          </div>

          <div v-else-if="selectedNode.nodeType === 'layer'" class="flex-1">
            <span class="inline-flex items-center gap-1 text-xs text-violet-700 font-medium mb-2">
              <Layers class="w-3.5 h-3.5" />
              协议分层
            </span>
            <h4 class="text-base font-semibold text-slate-900">{{ selectedNode.name }}</h4>
            <p class="mt-3 text-sm text-slate-600 leading-relaxed">
              该层包含
              {{
                graphData?.children?.find((l) => l.layerKey === selectedNode.layerKey)?.children?.length ?? 0
              }}
              个知识点，点击子节点查看详细内容。
            </p>
          </div>

          <div v-else class="flex-1">
            <h4 class="text-base font-semibold text-slate-900">{{ selectedNode.name }}</h4>
            <p class="mt-3 text-sm text-slate-600 leading-relaxed">
              TCP/IP 五层网络模型知识体系，向下展开可浏览各层知识点。
            </p>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>
