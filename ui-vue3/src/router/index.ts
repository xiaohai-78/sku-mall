import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/cards' },
  {
    path: '/cards',
    name: 'Cards',
    component: () => import('../pages/Cards/index.vue'),
    meta: { title: '点卡管理' }
  },
  {
    path: '/stock',
    name: 'Stock',
    component: () => import('../pages/Stock/index.vue'),
    meta: { title: '库存管理' }
  },
  {
    path: '/sales',
    name: 'Sales',
    component: () => import('../pages/Sales/index.vue'),
    meta: { title: '点卡销售' }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../pages/Orders/index.vue'),
    meta: { title: '订单管理' }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.afterEach((to) => {
  if (to.meta?.title) document.title = String(to.meta.title);
});

export default router; 