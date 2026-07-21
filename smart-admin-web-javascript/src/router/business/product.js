import SmartLayout from '/@/layout/index.vue';

export const productRouters = [
  {
    path: '/product',
    name: '_product',
    component: SmartLayout,
    meta: { title: '产品管理' },
    children: [
      {
        path: '/product/view',
        name: 'ProductView',
        meta: { title: '产品详情', hideInMenu: true },
        component: () => import('/@/views/business/product/product-view.vue'),
      },
    ],
  },
];
