<script setup>
import { computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { Network, Home, GitBranch, Route, Globe } from 'lucide-vue-next'

const route = useRoute()

const protocolLinks = [
  { to: '/protocol/tcp', label: '题目2 · TCP 握手/挥手', icon: GitBranch },
  { to: '/protocol/routing', label: '题目5 · 路由转发', icon: Route },
]

const isActive = (path) => route.path === path
const isProtocolSection = computed(() => route.path.startsWith('/protocol'))
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex flex-col">
    <header class="border-b border-slate-200 bg-white/95 backdrop-blur-sm sticky top-0 z-50 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 md:px-6">
        <div class="flex items-center justify-between h-14 gap-4">
          <RouterLink to="/" class="flex items-center gap-2 shrink-0 group">
            <Network class="w-5 h-5 text-teal-600 group-hover:text-teal-500 transition-colors" />
            <span class="font-semibold text-slate-900 text-sm md:text-base hidden sm:inline">
              计算机网络交互系统
            </span>
          </RouterLink>

          <nav class="flex items-center gap-1 md:gap-2" aria-label="主导航">
            <RouterLink
              to="/"
              class="nav-link"
              :class="{ 'nav-link-active': route.name === 'home' }"
            >
              <Home class="w-4 h-4" />
              <span class="hidden sm:inline">首页</span>
            </RouterLink>

            <div class="relative group/nav">
              <span
                class="nav-link cursor-default"
                :class="{ 'nav-link-active': isProtocolSection }"
              >
                <GitBranch class="w-4 h-4" />
                <span class="hidden sm:inline">协议可视化</span>
              </span>
              <div
                class="absolute right-0 top-full pt-1 opacity-0 invisible group-hover/nav:opacity-100 group-hover/nav:visible transition-all duration-150 z-50"
              >
                <div
                  class="bg-white border border-slate-200 rounded-xl shadow-lg py-1 min-w-[220px] overflow-hidden"
                >
                  <RouterLink
                    v-for="link in protocolLinks"
                    :key="link.to"
                    :to="link.to"
                    class="flex items-center gap-2 px-4 py-2.5 text-sm text-slate-700 hover:bg-teal-50 hover:text-teal-800 transition-colors"
                    :class="{ 'bg-teal-50 text-teal-800 font-medium': isActive(link.to) }"
                  >
                    <component :is="link.icon" class="w-4 h-4 shrink-0" />
                    {{ link.label }}
                  </RouterLink>
                </div>
              </div>
            </div>

            <RouterLink
              to="/scenario"
              class="nav-link"
              :class="{ 'nav-link-active': route.name === 'scenario' }"
            >
              <Globe class="w-4 h-4" />
              <span class="hidden sm:inline">综合场景</span>
            </RouterLink>
          </nav>
        </div>

        <!-- Mobile protocol tabs -->
        <div
          v-if="isProtocolSection"
          class="flex gap-2 pb-3 overflow-x-auto sm:hidden"
        >
          <RouterLink
            v-for="link in protocolLinks"
            :key="link.to"
            :to="link.to"
            class="shrink-0 px-3 py-1.5 rounded-full text-xs font-medium border transition-colors"
            :class="
              isActive(link.to)
                ? 'bg-teal-600 text-white border-teal-600'
                : 'bg-white text-slate-600 border-slate-200'
            "
          >
            {{ link.label.replace('题目2 · ', '').replace('题目5 · ', '') }}
          </RouterLink>
        </div>
      </div>
    </header>

    <main class="flex-1">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.nav-link {
  @apply inline-flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium
    text-slate-600 hover:text-slate-900 hover:bg-slate-100 transition-colors;
}
.nav-link-active {
  @apply text-teal-700 bg-teal-50 hover:bg-teal-50 hover:text-teal-700;
}
</style>
