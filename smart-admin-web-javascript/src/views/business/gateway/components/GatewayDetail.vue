<template>
  <a-drawer :open="visible" title="查看设备网关" :width="650" placement="right" :body-style="{ paddingBottom: '40px' }" @close="onClose">
    <a-spin :spinning="loading">
      <template v-if="detail">
        <a-descriptions :column="2" bordered size="small" title="基础信息">
          <a-descriptions-item label="网关名称">{{ detail.name }}</a-descriptions-item>
          <a-descriptions-item label="接入类型">{{ $smartEnumPlugin.getDescByValue('ACCESS_TYPE_ENUM', detail.type) }}</a-descriptions-item>
          <a-descriptions-item label="传输方式">{{ detail.transport || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ detail.createTime }}</a-descriptions-item>
          <a-descriptions-item label="描述" :span="2">{{ detail.description || '无' }}</a-descriptions-item>
        </a-descriptions>

        <a-descriptions :column="1" bordered size="small" title="网络组件信息" style="margin-top: 16px">
          <template v-if="detail.type === 'gateway_child'">
            <a-descriptions-item label="网络组件">无（其他网关设备接入）</a-descriptions-item>
          </template>
          <template v-else-if="detail.networkComponent">
            <a-descriptions-item label="组件名称">{{ detail.networkComponent.name }}</a-descriptions-item>
            <a-descriptions-item label="组件类型">{{
              $smartEnumPlugin.getDescByValue('COMPONENT_TYPE_ENUM', detail.networkComponent.type)
            }}</a-descriptions-item>
            <a-descriptions-item label="组件状态">
              <a-tag :color="detail.networkComponent.status === 1 ? 'green' : 'red'">{{
                detail.networkComponent.status === 1 ? '启用' : '禁用'
              }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item v-for="item in configItems" :key="item.key" :label="item.label">
              <a-tag v-if="item.isBoolean" :color="item.value ? 'green' : 'red'">{{ item.value ? '是' : '否' }}</a-tag>
              <template v-else>{{ item.value }}</template>
            </a-descriptions-item>
            <a-descriptions-item label="组件描述">{{ detail.networkComponent.description || '无' }}</a-descriptions-item>
          </template>
          <template v-else>
            <a-descriptions-item label="网络组件">-</a-descriptions-item>
          </template>
        </a-descriptions>

        <a-descriptions :column="1" bordered size="small" title="协议信息" style="margin-top: 16px">
          <template v-if="detail.protocol">
            <a-descriptions-item label="协议名称">{{ detail.protocol.name }}</a-descriptions-item>
            <a-descriptions-item label="协议版本">{{ detail.protocol.version }}</a-descriptions-item>
            <a-descriptions-item label="协议描述">{{ detail.protocol.description || '无' }}</a-descriptions-item>
          </template>
          <template v-else>
            <a-descriptions-item label="协议">-</a-descriptions-item>
          </template>
        </a-descriptions>
      </template>
      <div style="height: 40px"></div>
    </a-spin>
  </a-drawer>
</template>

<script setup>
  import { ref, computed } from 'vue';
  import { gatewayApi } from '/@/api/business/gateway/gateway-api';
  import { NETWORK_COMPONENT_CONFIG_LABEL_MAP } from '/@/constants/business/gateway/gateway-const';

  const configLabelMap = NETWORK_COMPONENT_CONFIG_LABEL_MAP;

  const visible = ref(false);
  const loading = ref(false);
  const detail = ref(null);

  const configItems = computed(() => {
    const nc = detail.value?.networkComponent;
    if (!nc || !nc.configuration) return [];
    let cfg = {};
    try {
      cfg = typeof nc.configuration === 'string' ? JSON.parse(nc.configuration) : nc.configuration;
    } catch (e) {
      return [];
    }
    return Object.entries(cfg).map(([key, value]) => ({
      key,
      label: configLabelMap[key] || key,
      value,
      isBoolean: key === 'tlsEnabled',
    }));
  });

  async function show(data) {
    visible.value = true;
    loading.value = true;
    try {
      const res = await gatewayApi.getDetail(data.id);
      detail.value = res.data;
    } catch (e) {
      detail.value = null;
    } finally {
      loading.value = false;
    }
  }

  function onClose() {
    visible.value = false;
    detail.value = null;
  }

  defineExpose({ show });
</script>
