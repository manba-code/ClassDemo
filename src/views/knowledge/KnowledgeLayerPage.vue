<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import KnowledgeLayerView from '../../components/KnowledgeLayerView.vue'

const route = useRoute()
const layerKey = computed(() => route.meta.layerKey)
</script>

<template>
  <KnowledgeLayerView v-if="layerKey" :layer-key="layerKey" />
</template>
