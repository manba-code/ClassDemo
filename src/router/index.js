import { createRouter, createWebHistory } from 'vue-router'

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
