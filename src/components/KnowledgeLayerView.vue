<script setup>
import { ref, watch, computed } from 'vue'
import { getLayer } from '../api/layers'
import {
  listKnowledge,
  createKnowledge,
  updateKnowledge,
  deleteKnowledge,
} from '../api/knowledge'
import {
  Plus,
  Pencil,
  Trash2,
  Search,
  Loader2,
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  X,
  BookOpen,
  Cpu,
  HardDrive,
} from 'lucide-vue-next'

const props = defineProps({
  layerKey: { type: String, required: true },
})

const layerInfo = ref(null)
const layerLoading = ref(true)
const layerError = ref('')

const knowledgeList = ref([])
const pagination = ref({ page: 1, pageSize: 10, total: 0, totalPages: 0 })
const listLoading = ref(false)
const listError = ref('')
const searchInput = ref('')

const showModal = ref(false)
const editingItem = ref(null)
const form = ref({ title: '', content: '', tags: '', sortOrder: 0 })
const formErrors = ref({ title: '', content: '' })
const submitting = ref(false)
const submitError = ref('')

const deleteTarget = ref(null)
const deleting = ref(false)

const isEditing = computed(() => editingItem.value !== null)
const modalTitle = computed(() => (isEditing.value ? '编辑知识点' : '新增知识点'))

function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function loadLayerInfo() {
  layerLoading.value = true
  layerError.value = ''
  try {
    layerInfo.value = await getLayer(props.layerKey)
  } catch (err) {
    layerInfo.value = null
    layerError.value = err.message || '加载层信息失败'
  } finally {
    layerLoading.value = false
  }
}

async function loadKnowledge(page = pagination.value.page) {
  listLoading.value = true
  listError.value = ''
  try {
    const data = await listKnowledge(props.layerKey, {
      page,
      pageSize: pagination.value.pageSize,
      keyword: searchInput.value.trim(),
    })
    knowledgeList.value = data.list ?? []
    pagination.value = data.pagination ?? pagination.value
  } catch (err) {
    knowledgeList.value = []
    listError.value = err.message || '加载知识点列表失败'
  } finally {
    listLoading.value = false
  }
}

function handleSearch() {
  pagination.value = { ...pagination.value, page: 1 }
  loadKnowledge(1)
}

function goToPage(page) {
  if (page < 1 || page > pagination.value.totalPages) return
  loadKnowledge(page)
}

function resetForm() {
  form.value = { title: '', content: '', tags: '', sortOrder: 0 }
  formErrors.value = { title: '', content: '' }
  submitError.value = ''
}

function openCreateModal() {
  editingItem.value = null
  resetForm()
  showModal.value = true
}

function openEditModal(item) {
  editingItem.value = item
  form.value = {
    title: item.title,
    content: item.content,
    tags: item.tags ?? '',
    sortOrder: item.sortOrder ?? 0,
  }
  formErrors.value = { title: '', content: '' }
  submitError.value = ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingItem.value = null
  resetForm()
}

function validateForm() {
  const errors = { title: '', content: '' }
  if (!form.value.title.trim()) errors.title = '标题不能为空'
  if (!form.value.content.trim()) errors.content = '内容不能为空'
  formErrors.value = errors
  return !errors.title && !errors.content
}

async function handleSubmit() {
  if (!validateForm()) return
  submitting.value = true
  submitError.value = ''
  const body = {
    title: form.value.title.trim(),
    content: form.value.content.trim(),
    tags: form.value.tags.trim() || undefined,
    sortOrder: Number(form.value.sortOrder) || 0,
  }
  const wasEditing = isEditing.value
  const currentPage = pagination.value.page
  try {
    if (wasEditing) {
      await updateKnowledge(props.layerKey, editingItem.value.id, body)
    } else {
      await createKnowledge(props.layerKey, body)
    }
    closeModal()
    await loadKnowledge(wasEditing ? currentPage : 1)
  } catch (err) {
    submitError.value = err.message || '保存失败'
  } finally {
    submitting.value = false
  }
}

function confirmDelete(item) {
  deleteTarget.value = item
}

function cancelDelete() {
  deleteTarget.value = null
}

async function handleDelete() {
  if (!deleteTarget.value) return
  deleting.value = true
  try {
    await deleteKnowledge(props.layerKey, deleteTarget.value.id)
    deleteTarget.value = null
    const { page, totalPages } = pagination.value
    const nextPage =
      knowledgeList.value.length === 1 && page > 1 ? page - 1 : page
    await loadKnowledge(Math.min(nextPage, totalPages || 1))
  } catch (err) {
    listError.value = err.message || '删除失败'
    deleteTarget.value = null
  } finally {
    deleting.value = false
  }
}

async function reloadAll() {
  searchInput.value = ''
  pagination.value = { page: 1, pageSize: 10, total: 0, totalPages: 0 }
  await Promise.all([loadLayerInfo(), loadKnowledge(1)])
}

watch(
  () => props.layerKey,
  () => reloadAll(),
  { immediate: true },
)
</script>

<template>
  <div class="space-y-6">
    <!-- 层元数据：KN-01 ~ KN-03 -->
    <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <div v-if="layerLoading" class="flex items-center gap-2 text-sm text-slate-500">
        <Loader2 class="w-4 h-4 animate-spin" />
        加载层信息…
      </div>
      <div
        v-else-if="layerError"
        class="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
      >
        <AlertCircle class="w-4 h-4 shrink-0 mt-0.5" />
        {{ layerError }}
      </div>
      <template v-else-if="layerInfo">
        <h2 class="text-lg font-semibold text-slate-900">{{ layerInfo.layerName }}</h2>
        <div class="mt-4 grid gap-4 md:grid-cols-3">
          <div class="rounded-xl border border-violet-100 bg-violet-50/40 p-4">
            <div class="flex items-center gap-2 text-sm font-medium text-violet-800 mb-2">
              <BookOpen class="w-4 h-4" />
              主要功能
            </div>
            <p class="text-sm text-slate-700 leading-relaxed">{{ layerInfo.mainFunction }}</p>
          </div>
          <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
            <div class="flex items-center gap-2 text-sm font-medium text-slate-800 mb-2">
              <Cpu class="w-4 h-4" />
              常见协议或技术
            </div>
            <div class="flex flex-wrap gap-1.5">
              <span
                v-for="proto in layerInfo.protocols"
                :key="proto"
                class="inline-block rounded-md bg-white border border-slate-200 px-2 py-0.5 text-xs font-medium text-slate-700"
              >
                {{ proto }}
              </span>
              <span v-if="!layerInfo.protocols?.length" class="text-sm text-slate-400">暂无</span>
            </div>
          </div>
          <div class="rounded-xl border border-slate-200 bg-slate-50/60 p-4">
            <div class="flex items-center gap-2 text-sm font-medium text-slate-800 mb-2">
              <HardDrive class="w-4 h-4" />
              相关设备或数据单位
            </div>
            <p class="text-sm text-slate-700 leading-relaxed">{{ layerInfo.devicesUnits }}</p>
          </div>
        </div>
      </template>
    </section>

    <!-- 知识点列表：KN-04 ~ KN-08 -->
    <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-4">
        <h3 class="text-base font-semibold text-slate-900">知识点列表</h3>
        <div class="flex flex-wrap items-center gap-2">
          <div class="relative flex-1 sm:flex-none">
            <Search class="absolute left-2.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              v-model="searchInput"
              type="search"
              placeholder="搜索标题、内容、标签…"
              class="w-full sm:w-56 pl-8 pr-3 py-1.5 text-sm rounded-lg border border-slate-200
                focus:outline-none focus:ring-2 focus:ring-violet-200 focus:border-violet-300"
              @keydown.enter="handleSearch"
            />
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium rounded-lg
              border border-slate-200 text-slate-700 hover:bg-slate-50 transition-colors"
            @click="handleSearch"
          >
            <Search class="w-3.5 h-3.5" />
            搜索
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium rounded-lg
              bg-violet-600 text-white hover:bg-violet-700 transition-colors"
            @click="openCreateModal"
          >
            <Plus class="w-3.5 h-3.5" />
            新增
          </button>
        </div>
      </div>

      <div v-if="listLoading" class="flex items-center justify-center gap-2 py-12 text-sm text-slate-500">
        <Loader2 class="w-4 h-4 animate-spin" />
        加载知识点…
      </div>

      <div
        v-else-if="listError"
        class="flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
      >
        <AlertCircle class="w-4 h-4 shrink-0 mt-0.5" />
        {{ listError }}
      </div>

      <div
        v-else-if="knowledgeList.length === 0"
        class="rounded-xl border border-dashed border-slate-200 py-12 text-center text-sm text-slate-500"
      >
        {{ searchInput.trim() ? '未找到匹配的知识点' : '暂无知识点，点击「新增」添加' }}
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead>
            <tr class="border-b border-slate-200 text-left text-slate-500">
              <th class="pb-2 pr-4 font-medium">标题</th>
              <th class="pb-2 pr-4 font-medium hidden md:table-cell">标签</th>
              <th class="pb-2 pr-4 font-medium hidden lg:table-cell">更新时间</th>
              <th class="pb-2 font-medium text-right">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in knowledgeList"
              :key="item.id"
              class="border-b border-slate-100 last:border-0 hover:bg-slate-50/80"
            >
              <td class="py-3 pr-4">
                <p class="font-medium text-slate-900">{{ item.title }}</p>
                <p class="mt-0.5 text-xs text-slate-500 line-clamp-2 md:hidden">{{ item.content }}</p>
              </td>
              <td class="py-3 pr-4 hidden md:table-cell">
                <span v-if="item.tags" class="text-xs text-slate-600">{{ item.tags }}</span>
                <span v-else class="text-xs text-slate-400">—</span>
              </td>
              <td class="py-3 pr-4 hidden lg:table-cell text-xs text-slate-500 whitespace-nowrap">
                {{ formatDate(item.updatedAt) }}
              </td>
              <td class="py-3 text-right whitespace-nowrap">
                <button
                  type="button"
                  class="inline-flex items-center gap-1 px-2 py-1 text-xs font-medium rounded
                    text-slate-600 hover:bg-slate-100 transition-colors"
                  title="编辑"
                  @click="openEditModal(item)"
                >
                  <Pencil class="w-3.5 h-3.5" />
                  编辑
                </button>
                <button
                  type="button"
                  class="inline-flex items-center gap-1 px-2 py-1 text-xs font-medium rounded
                    text-red-600 hover:bg-red-50 transition-colors ml-1"
                  title="删除"
                  @click="confirmDelete(item)"
                >
                  <Trash2 class="w-3.5 h-3.5" />
                  删除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div
        v-if="pagination.totalPages > 0"
        class="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-slate-100 pt-4"
      >
        <p class="text-xs text-slate-500">
          共 {{ pagination.total }} 条，第 {{ pagination.page }} / {{ pagination.totalPages }} 页
        </p>
        <div class="flex items-center gap-1">
          <button
            type="button"
            class="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-lg
              border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed"
            :disabled="pagination.page <= 1 || listLoading"
            @click="goToPage(pagination.page - 1)"
          >
            <ChevronLeft class="w-3.5 h-3.5" />
            上一页
          </button>
          <button
            type="button"
            class="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-lg
              border border-slate-200 text-slate-600 hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed"
            :disabled="pagination.page >= pagination.totalPages || listLoading"
            @click="goToPage(pagination.page + 1)"
          >
            下一页
            <ChevronRight class="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </section>

    <!-- 新增/编辑弹窗：KN-05、KN-06、G-11 -->
    <Teleport to="body">
      <div
        v-if="showModal"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="closeModal"
      >
        <div
          class="w-full max-w-lg rounded-2xl bg-white shadow-xl border border-slate-200"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="'knowledge-modal-title'"
        >
          <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100">
            <h4 id="knowledge-modal-title" class="text-base font-semibold text-slate-900">
              {{ modalTitle }}
            </h4>
            <button
              type="button"
              class="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              aria-label="关闭"
              @click="closeModal"
            >
              <X class="w-5 h-5" />
            </button>
          </div>
          <form class="px-6 py-4 space-y-4" @submit.prevent="handleSubmit">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">
                标题 <span class="text-red-500">*</span>
              </label>
              <input
                v-model="form.title"
                type="text"
                maxlength="255"
                class="w-full px-3 py-2 text-sm rounded-lg border transition-colors"
                :class="formErrors.title ? 'border-red-300 focus:ring-red-200' : 'border-slate-200 focus:ring-violet-200 focus:border-violet-300'"
                placeholder="知识点标题"
                @input="formErrors.title = ''"
              />
              <p v-if="formErrors.title" class="mt-1 text-xs text-red-600">{{ formErrors.title }}</p>
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">
                内容 <span class="text-red-500">*</span>
              </label>
              <textarea
                v-model="form.content"
                rows="5"
                class="w-full px-3 py-2 text-sm rounded-lg border transition-colors resize-y"
                :class="formErrors.content ? 'border-red-300 focus:ring-red-200' : 'border-slate-200 focus:ring-violet-200 focus:border-violet-300'"
                placeholder="详细内容"
                @input="formErrors.content = ''"
              />
              <p v-if="formErrors.content" class="mt-1 text-xs text-red-600">{{ formErrors.content }}</p>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">标签</label>
                <input
                  v-model="form.tags"
                  type="text"
                  class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200
                    focus:outline-none focus:ring-2 focus:ring-violet-200 focus:border-violet-300"
                  placeholder="逗号分隔，如 HTTP,Web"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">排序权重</label>
                <input
                  v-model.number="form.sortOrder"
                  type="number"
                  min="0"
                  class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200
                    focus:outline-none focus:ring-2 focus:ring-violet-200 focus:border-violet-300"
                />
              </div>
            </div>
            <p v-if="submitError" class="text-sm text-red-600">{{ submitError }}</p>
            <div class="flex justify-end gap-2 pt-2">
              <button
                type="button"
                class="px-4 py-2 text-sm font-medium rounded-lg border border-slate-200 text-slate-700 hover:bg-slate-50"
                :disabled="submitting"
                @click="closeModal"
              >
                取消
              </button>
              <button
                type="submit"
                class="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg
                  bg-violet-600 text-white hover:bg-violet-700 disabled:opacity-60"
                :disabled="submitting"
              >
                <Loader2 v-if="submitting" class="w-4 h-4 animate-spin" />
                {{ submitting ? '保存中…' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 删除确认：KN-07 -->
      <div
        v-if="deleteTarget"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40"
        @click.self="cancelDelete"
      >
        <div class="w-full max-w-sm rounded-2xl bg-white shadow-xl border border-slate-200 p-6">
          <h4 class="text-base font-semibold text-slate-900">确认删除</h4>
          <p class="mt-2 text-sm text-slate-600">
            确定删除知识点「<span class="font-medium text-slate-900">{{ deleteTarget.title }}</span>」？此操作不可撤销。
          </p>
          <div class="mt-5 flex justify-end gap-2">
            <button
              type="button"
              class="px-4 py-2 text-sm font-medium rounded-lg border border-slate-200 text-slate-700 hover:bg-slate-50"
              :disabled="deleting"
              @click="cancelDelete"
            >
              取消
            </button>
            <button
              type="button"
              class="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg
                bg-red-600 text-white hover:bg-red-700 disabled:opacity-60"
              :disabled="deleting"
              @click="handleDelete"
            >
              <Loader2 v-if="deleting" class="w-4 h-4 animate-spin" />
              {{ deleting ? '删除中…' : '删除' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
