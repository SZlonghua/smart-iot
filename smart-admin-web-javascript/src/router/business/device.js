import SmartLayout from '/@/layout/index.vue';

export const deviceRouters = [
  {
    path: '/device',
    name: '_device',
    component: SmartLayout,
    meta: { title: '设备管理' },
    children: [
      {
        path: '/device/view',
        name: 'DeviceView',
        meta: { title: '设备详情', hideInMenu: true },
        component: () => import('/@/views/business/device/device-view.vue'),
      },
    ],
  },
];
