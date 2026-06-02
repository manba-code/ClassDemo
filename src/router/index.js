import { createRouter, createWebHistory } from 'vue-router'

const knowledgeLayerRoutes = [
  {
    path: 'application',
    name: 'knowledge-application',
    component: () => import('../views/knowledge/KnowledgeLayerPage.vue'),
    meta: { title: '应用层 · 知识体系', layerKey: 'application', nav: 'knowledge' },
  },
  {
    path: 'transport',
    name: 'knowledge-transport',
    component: () => import('../views/knowledge/KnowledgeLayerPage.vue'),
    meta: { title: '传输层 · 知识体系', layerKey: 'transport', nav: 'knowledge' },
  },
  {
    path: 'network',
    name: 'knowledge-network',
    component: () => import('../views/knowledge/KnowledgeLayerPage.vue'),
    meta: { title: '网络层 · 知识体系', layerKey: 'network', nav: 'knowledge' },
  },
  {
    path: 'datalink',
    name: 'knowledge-datalink',
    component: () => import('../views/knowledge/KnowledgeLayerPage.vue'),
    meta: { title: '数据链路层 · 知识体系', layerKey: 'datalink', nav: 'knowledge' },
  },
  {
    path: 'physical',
    name: 'knowledge-physical',
    component: () => import('../views/knowledge/KnowledgeLayerPage.vue'),
    meta: { title: '物理层 · 知识体系', layerKey: 'physical', nav: 'knowledge' },
  },
  {
    path: 'graph',
    name: 'knowledge-graph',
    component: () => import('../views/knowledge/KnowledgeGraphView.vue'),
    meta: { title: '知识图谱 · 知识体系', nav: 'knowledge' },
  },
  {
    path: 'search',
    name: 'knowledge-search',
    component: () => import('../views/knowledge/KnowledgeSearchView.vue'),
    meta: { title: '知识检索 · 知识体系', nav: 'knowledge' },
  },
]

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../views/HomeView.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/protocol/tcp',
    name: 'protocol-tcp',
    component: () => import('../views/protocol/TcpView.vue'),
    meta: { title: 'TCP 三次握手 / 四次挥手', nav: 'protocol' },
  },
  {
    path: '/protocol/routing',
    name: 'protocol-routing',
    component: () => import('../views/protocol/RoutingView.vue'),
    meta: { title: '路由转发', nav: 'protocol' },
  },
  {
    path: '/scenario',
    name: 'scenario',
    component: () => import('../views/ScenarioView.vue'),
    meta: { title: '综合网络场景模拟', nav: 'scenario' },
  },
  {
    path: '/knowledge',
    component: () => import('../layouts/KnowledgeLayout.vue'),
    redirect: '/knowledge/application',
    children: knowledgeLayerRoutes,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.afterEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} · 计算机网络交互系统`
    : '计算机网络交互系统'
})

export default router
