<script setup>
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Loader2 } from 'lucide-vue-next'
import { searchKnowledge } from '../../api/knowledgeSearch.js'
import { getKnowledge } from '../../api/knowledge.js'

const router = useRouter()

const keyword = ref('')
const loading = ref(false)
const error = ref('')
const results = ref(null)
const expandedId = ref(null)
const page = ref(1)
const pageSize = 10

const suggestedQuestions = [
  '什么是三次握手',
  'ARP 作用是什么',
  'DNS 解析过程',
  'TCP 和 UDP 区别',
  '路由转发原理',
]

const exampleKeywords = ['TCP', 'ARP', 'DNS', 'HTTP', '路由']

const layerColors = {
  application: 'bg-violet-100 text-violet-800 border-violet-200',
  transport: 'bg-teal-100 text-teal-800 border-teal-200',
  network: 'bg-indigo-100 text-indigo-800 border-indigo-200',
  datalink: 'bg-amber-100 text-amber-800 border-amber-200',
  physical: 'bg-slate-100 text-slate-700 border-slate-200',
}

async function doSearch(kw = keyword.value, p = 1) {
  const q = (kw || '').trim()
  if (!q) return

  keyword.value = q
  page.value = p
  loading.value = true
  error.value = ''
  expandedId.value = null

  try {
    results.value = await searchKnowledge({ keyword: q, page: p, pageSize })
  } catch (e) {
    error.value = e.message || '检索失败'
    results.value = null
  } finally {
    loading.value = false
  }
}

function highlightText(text, kw) {
  if (!text || !kw) return text
  const escaped = kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const re = new RegExp(`(${escaped})`, 'gi')
  return text.replace(re, '<mark class="bg-yellow-200 px-0.5 rounded">$1</mark>')
}

const fullContents = ref({})

async function toggleExpand(item) {
  const key = `${item.layerKey}-${item.id}`
  if (expandedId.value === key) {
    expandedId.value = null
    return
  }
  expandedId.value = key
  if (!fullContents.value[key]) {
    try {
      const detail = await getKnowledge(item.layerKey, item.id)
      fullContents.value[key] = detail.content
    } catch {
      fullContents.value[key] = item.snippet
    }
  }
}

function fullContent(item) {
  return fullContents.value[itemKey(item)] ?? item.snippet
}

function itemKey(item) {
  return `${item.layerKey}-${item.id}`
}

function goToLayer(item) {
  router.push(`/knowledge/${item.layerKey}`)
}

const groupedResults = computed(() => {
  if (!results.value?.list?.length) return []
  const groups = {}
  for (const item of results.value.list) {
    if (!groups[item.layerKey]) {
      groups[item.layerKey] = { layerKey: item.layerKey, layerName: item.layerName, items: [] }
    }
    groups[item.layerKey].items.push(item)
  }
  return Object.values(groups)
})

onMounted(() => {
  const q = router.currentRoute.value.query.q
  if (q) doSearch(String(q))
})
</script>

<template>
  <div class="space-y-6">
    <div>
      <h2 class="text-lg font-semibold text-slate-900 mb-1">知识检索问答</h2>
      <p class="text-sm text-slate-500">跨五层知识点联合检索，输入问题或关键词即可匹配</p>
    </div>

    <!-- Search box -->
    <form class="flex gap-2" @submit.prevent="doSearch()">
      <input
        v-model="keyword"
        type="search"
        placeholder="例如：什么是三次握手？"
        class="flex-1 px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-violet-300 focus:border-violet-400"
      />
      <button
        type="submit"
        class="px-4 py-2.5 rounded-xl bg-violet-600 hover:bg-violet-500 text-white text-sm font-medium inline-flex items-center gap-2 disabled:opacity-50"
        :disabled="loading || !keyword.trim()"
      >
        <Loader2 v-if="loading" class="w-4 h-4 animate-spin" />
        <Search v-else class="w-4 h-4" />
        搜索
      </button>
    </form>

    <!-- Suggested chips -->
    <div class="flex flex-wrap gap-2">
      <span class="text-xs text-slate-400 self-center">推荐问题：</span>
      <button
        v-for="q in suggestedQuestions"
        :key="q"
        type="button"
        class="px-3 py-1 rounded-full text-xs border border-slate-200 text-slate-600 hover:bg-violet-50 hover:border-violet-200 hover:text-violet-800 transition-colors"
        @click="doSearch(q)"
      >
        {{ q }}
      </button>
    </div>

    <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

    <!-- Results -->
    <template v-if="results">
      <div
        v-if="results.list.length"
        class="rounded-xl border border-violet-200 bg-violet-50/50 p-4 text-sm text-violet-900"
      >
        根据知识点库，与「<strong>{{ results.keyword }}</strong>」相关的内容如下（共
        {{ results.pagination.total }} 条）：
      </div>

      <div v-if="!results.list.length" class="rounded-xl border border-slate-200 bg-slate-50 p-8 text-center">
        <p class="text-slate-600">未找到相关知识点</p>
        <p class="text-xs text-slate-400 mt-2">
          试试示例关键词：
          <button
            v-for="kw in exampleKeywords"
            :key="kw"
            type="button"
            class="mx-1 text-violet-600 hover:underline"
            @click="doSearch(kw)"
          >
            {{ kw }}
          </button>
        </p>
      </div>

      <div v-else class="space-y-6">
        <section v-for="group in groupedResults" :key="group.layerKey" class="space-y-3">
          <h3 class="text-sm font-semibold text-slate-700 flex items-center gap-2">
            <span
              class="px-2 py-0.5 rounded text-xs border"
              :class="layerColors[group.layerKey] ?? 'bg-slate-100 text-slate-700'"
            >
              {{ group.layerName }}
            </span>
          </h3>

          <article
            v-for="item in group.items"
            :key="itemKey(item)"
            class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm hover:border-violet-200 transition-colors"
          >
            <div class="flex items-start justify-between gap-3">
              <h4 class="text-base font-medium text-slate-900">{{ item.title }}</h4>
              <button
                type="button"
                class="shrink-0 text-xs text-violet-600 hover:underline"
                @click="goToLayer(item)"
              >
                查看该层 →
              </button>
            </div>

            <p
              v-if="item.tags"
              class="text-[10px] text-slate-400 font-mono mt-1"
              v-html="highlightText(item.tags, keyword)"
            />

            <p
              class="text-sm text-slate-600 mt-2 leading-relaxed"
              v-html="highlightText(item.snippet, keyword)"
            />

            <button
              v-if="expandedId !== itemKey(item)"
              type="button"
              class="mt-2 text-xs text-violet-600 hover:underline"
              @click="toggleExpand(item)"
            >
              展开完整内容
            </button>

            <div
              v-else
              class="mt-3 pt-3 border-t border-slate-100 text-sm text-slate-700 leading-relaxed whitespace-pre-wrap"
              v-html="highlightText(fullContent(item), keyword)"
            />
          </article>
        </section>

        <!-- Pagination -->
        <div
          v-if="results.pagination.totalPages > 1"
          class="flex items-center justify-center gap-2 pt-2"
        >
          <button
            type="button"
            class="px-3 py-1.5 rounded-lg text-sm border border-slate-200 disabled:opacity-40"
            :disabled="page <= 1 || loading"
            @click="doSearch(keyword, page - 1)"
          >
            上一页
          </button>
          <span class="text-xs text-slate-500 font-mono">
            {{ page }} / {{ results.pagination.totalPages }}
          </span>
          <button
            type="button"
            class="px-3 py-1.5 rounded-lg text-sm border border-slate-200 disabled:opacity-40"
            :disabled="page >= results.pagination.totalPages || loading"
            @click="doSearch(keyword, page + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
