<script setup>
import { Table2 } from 'lucide-vue-next'

defineProps({
  title: String,
  routerLabel: String,
  rows: { type: Array, default: () => [] },
  highlightIndex: { type: Number, default: -1 },
  isActive: Boolean,
})
</script>

<template>
  <div
    class="rounded-xl border bg-white overflow-hidden shadow-sm transition-all duration-300"
    :class="isActive ? 'border-amber-300 ring-2 ring-amber-100' : 'border-slate-200'"
  >
    <div class="px-4 pt-3 pb-2 flex items-center justify-between">
      <h2 class="text-xs font-medium text-slate-500 uppercase tracking-wider flex items-center gap-1.5">
        <Table2 class="w-3.5 h-3.5" />
        {{ title }}
      </h2>
      <span
        v-if="isActive"
        class="text-[10px] font-mono px-2 py-0.5 rounded-full bg-amber-100 text-amber-800"
      >
        查表中
      </span>
    </div>
    <p class="px-4 pb-2 text-[10px] font-mono text-slate-400">{{ routerLabel }}</p>
    <div class="overflow-x-auto">
      <table class="w-full text-[10px] font-mono">
        <thead>
          <tr class="border-y border-slate-200 bg-slate-50 text-left">
            <th class="px-3 py-2 text-slate-600">目的网络</th>
            <th class="px-3 py-2 text-slate-600">掩码</th>
            <th class="px-3 py-2 text-slate-600">下一跳</th>
            <th class="px-3 py-2 text-slate-600">接口</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, i) in rows"
            :key="i"
            class="border-b border-slate-100 transition-colors duration-300"
            :class="i === highlightIndex ? 'bg-amber-100 text-amber-900' : ''"
          >
            <td class="px-3 py-2">{{ row.network }}</td>
            <td class="px-3 py-2 text-slate-500">{{ row.mask }}</td>
            <td class="px-3 py-2 font-semibold">{{ row.nextHop }}</td>
            <td class="px-3 py-2 text-slate-500">{{ row.iface }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
