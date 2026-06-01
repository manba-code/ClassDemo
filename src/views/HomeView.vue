<script setup>
import { RouterLink } from 'vue-router'
import { GitBranch, Route, ArrowRight, Layers, Globe } from 'lucide-vue-next'

const modules = [
  {
    title: '题目 2 · TCP 三次握手 / 四次挥手',
    desc: '动态演示 SYN、ACK、FIN 报文传递与 TCP 状态机变化，支持建立连接与释放连接分步操作。',
    to: '/protocol/tcp',
    icon: GitBranch,
    color: 'teal',
    status: '已完成',
  },
  {
    title: '题目 5 · 路由转发',
    desc: '跨网段数据包转发过程，展示路由器查表、下一跳选择与路由表动态高亮。',
    to: '/protocol/routing',
    icon: Route,
    color: 'indigo',
    status: '已完成',
  },
  {
    title: '综合网络场景模拟',
    desc: 'H1 访问 www.abc.com 从 t0 到 t1 的完整通信过程（第二部分，待开发）。',
    to: null,
    icon: Globe,
    color: 'slate',
    status: '待开发',
  },
  {
    title: '知识体系学习模块',
    desc: 'TCP/IP 五层模型知识点管理与知识图谱（第三部分，待开发）。',
    to: null,
    icon: Layers,
    color: 'slate',
    status: '待开发',
  },
]

const colorMap = {
  teal: {
    card: 'border-teal-200 hover:border-teal-300 hover:shadow-teal-100',
    icon: 'bg-teal-100 text-teal-700',
    badge: 'bg-teal-100 text-teal-700',
  },
  indigo: {
    card: 'border-indigo-200 hover:border-indigo-300 hover:shadow-indigo-100',
    icon: 'bg-indigo-100 text-indigo-700',
    badge: 'bg-indigo-100 text-indigo-700',
  },
  slate: {
    card: 'border-slate-200 opacity-75',
    icon: 'bg-slate-100 text-slate-500',
    badge: 'bg-slate-100 text-slate-500',
  },
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 md:px-6 py-10 md:py-14">
    <section class="text-center mb-12">
      <h1 class="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight">
        计算机网络知识体系交互式展示系统
      </h1>
      <p class="mt-3 text-slate-500 max-w-2xl mx-auto text-sm md:text-base">
        通过顶部导航或下方卡片进入对应演示页面。
      </p>
    </section>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
      <component
        :is="mod.to ? RouterLink : 'div'"
        v-for="mod in modules"
        :key="mod.title"
        v-bind="mod.to ? { to: mod.to } : {}"
        class="rounded-2xl border bg-white p-6 shadow-sm transition-all duration-200"
        :class="[
          colorMap[mod.color].card,
          mod.to ? 'hover:shadow-md cursor-pointer group' : 'cursor-default',
        ]"
      >
        <div class="flex items-start justify-between gap-3 mb-4">
          <div
            class="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
            :class="colorMap[mod.color].icon"
          >
            <component :is="mod.icon" class="w-5 h-5" />
          </div>
          <span
            class="text-[10px] font-medium px-2 py-1 rounded-full"
            :class="colorMap[mod.color].badge"
          >
            {{ mod.status }}
          </span>
        </div>
        <h2 class="font-semibold text-slate-900 group-hover:text-teal-800 transition-colors">
          {{ mod.title }}
        </h2>
        <p class="text-sm text-slate-500 mt-2 leading-relaxed">{{ mod.desc }}</p>
        <p
          v-if="mod.to"
          class="mt-4 inline-flex items-center gap-1 text-sm font-medium text-teal-600 group-hover:gap-2 transition-all"
        >
          进入演示
          <ArrowRight class="w-4 h-4" />
        </p>
      </component>
    </div>
  </div>
</template>
