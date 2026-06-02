<script setup>
import { useRoute, RouterLink, RouterView } from 'vue-router'
import { Layers, GitGraph } from 'lucide-vue-next'

const route = useRoute()

const layerTabs = [
  { to: '/knowledge/application', label: '应用层', name: 'knowledge-application' },
  { to: '/knowledge/transport', label: '传输层', name: 'knowledge-transport' },
  { to: '/knowledge/network', label: '网络层', name: 'knowledge-network' },
  { to: '/knowledge/datalink', label: '数据链路层', name: 'knowledge-datalink' },
  { to: '/knowledge/physical', label: '物理层', name: 'knowledge-physical' },
]

const graphTab = { to: '/knowledge/graph', label: '知识图谱', name: 'knowledge-graph' }

function isTabActive(tab) {
  return route.name === tab.name
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 md:px-6 py-6 md:py-8">
    <div class="flex items-center gap-2 mb-4">
      <Layers class="w-5 h-5 text-violet-600 shrink-0" />
      <h1 class="text-lg md:text-xl font-semibold text-slate-900">TCP/IP 知识体系</h1>
    </div>

    <nav
      class="flex flex-wrap gap-2 mb-6 border-b border-slate-200 pb-3"
      aria-label="五层模型与知识图谱"
    >
      <RouterLink
        v-for="tab in layerTabs"
        :key="tab.name"
        :to="tab.to"
        class="knowledge-tab"
        :class="{ 'knowledge-tab-active': isTabActive(tab) }"
      >
        {{ tab.label }}
      </RouterLink>
      <span class="hidden sm:inline w-px h-6 bg-slate-200 self-center mx-1" aria-hidden="true" />
      <RouterLink
        :to="graphTab.to"
        class="knowledge-tab inline-flex items-center gap-1.5"
        :class="{ 'knowledge-tab-active': isTabActive(graphTab) }"
      >
        <GitGraph class="w-3.5 h-3.5" />
        {{ graphTab.label }}
      </RouterLink>
    </nav>

    <RouterView />
  </div>
</template>

<style scoped>
.knowledge-tab {
  @apply shrink-0 px-3 py-1.5 rounded-lg text-sm font-medium text-slate-600
    border border-transparent hover:text-slate-900 hover:bg-slate-100 transition-colors;
}
.knowledge-tab-active {
  @apply text-violet-800 bg-violet-50 border-violet-200 hover:bg-violet-50 hover:text-violet-800;
}
</style>
