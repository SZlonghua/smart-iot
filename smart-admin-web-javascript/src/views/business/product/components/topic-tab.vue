<!-- 产品 Topic 类列表（硬编码，后续和设备通信时改造） -->
<template>
  <div>
    <h4 style="margin-bottom: 12px">下行Topic（平台 → 设备）</h4>
    <a-table size="small" :dataSource="downTopics" :columns="columns" rowKey="name" bordered :pagination="false" style="margin-bottom: 24px" />

    <h4 style="margin-bottom: 12px">上行Topic（设备 → 平台）</h4>
    <a-table size="small" :dataSource="upTopics" :columns="columns" rowKey="name" bordered :pagination="false" />
  </div>
</template>

<script setup>
  import { computed } from 'vue';

  const props = defineProps({ productKey: { type: String, default: '' } });

  const columns = [
    { title: '名称', dataIndex: 'name', width: 180 },
    { title: 'Topic', dataIndex: 'topic', ellipsis: true },
  ];

  // 当前产品 Key，无则保留占位符
  const pk = computed(() => props.productKey || '{productKey}');

  // 下行 Topic：平台下发数据到设备
  const downTopics = computed(() => [
    { name: '属性读取', topic: `/${pk.value}/{deviceKey}/properties/read` },
    { name: '属性读取回复', topic: `/${pk.value}/{deviceKey}/properties/read/reply` },
    { name: '属性写入', topic: `/${pk.value}/{deviceKey}/properties/write` },
    { name: '属性写入回复', topic: `/${pk.value}/{deviceKey}/properties/write/reply` },
    { name: '功能调用', topic: `/${pk.value}/{deviceKey}/function/invoke` },
    { name: '功能调用回复', topic: `/${pk.value}/{deviceKey}/function/invoke/reply` },
  ]);

  // 上行 Topic：设备上报数据到平台
  const upTopics = computed(() => [
    { name: '属性上报', topic: `/${pk.value}/{deviceKey}/properties/report` },
    { name: '事件上报', topic: `/${pk.value}/{deviceKey}/event/{eventId}` },
    { name: '设备上线', topic: `/${pk.value}/{deviceKey}/online` },
    { name: '设备离线', topic: `/${pk.value}/{deviceKey}/offline` },
    { name: '设备注册', topic: `/${pk.value}/{deviceKey}/register` },
    { name: '设备注销', topic: `/${pk.value}/{deviceKey}/unregister` },
  ]);
</script>
