<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const layerKey = computed(() => route.meta.layerKey)
const layerTitle = computed(() => route.meta.title)
</script>

<template>
  <div class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
    <h2 class="text-base font-semibold text-slate-900">{{ layerTitle }}</h2>
    <p class="mt-2 text-sm text-slate-500">
      本层知识点 CRUD 与层元数据展示将在阶段 G 实现（<code class="text-xs bg-slate-100 px-1 rounded">layerKey={{ layerKey }}</code>）。
    </p>
  </div>
</template>
