<template>
  <div>
    <a-empty v-if="!gatewayDetail" description="未关联设备网关" />
    <template v-else>
      <!-- 基础信息 -->
      <a-card size="small" title="基础信息" style="margin-bottom: 12px">
        <a-descriptions size="small" :column="3" bordered>
          <a-descriptions-item label="网关名称">{{ gatewayDetail.name }}</a-descriptions-item>
          <a-descriptions-item label="接入类型">{{ $smartEnumPlugin.getDescByValue('ACCESS_TYPE_ENUM', gatewayDetail.type) }}</a-descriptions-item>
          <a-descriptions-item label="传输方式">{{ gatewayDetail.transport || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ gatewayDetail.createTime }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="gatewayDetail.status === 1 ? 'green' : 'red'">{{ gatewayDetail.status === 1 ? '启用' : '禁用' }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="描述">{{ gatewayDetail.description || '无' }}</a-descriptions-item>
        </a-descriptions>
      </a-card>

      <!-- 网络组件信息 -->
      <a-card size="small" title="网络组件信息" style="margin-bottom: 12px">
        <template v-if="gatewayDetail.type === 'gateway_child'">
          <a-descriptions size="small" :column="1" bordered>
            <a-descriptions-item label="网络组件">无（其他网关设备接入）</a-descriptions-item>
          </a-descriptions>
        </template>
        <template v-else-if="gatewayDetail.networkComponent">
          <a-descriptions size="small" :column="3" bordered>
            <a-descriptions-item label="组件名称">{{ gatewayDetail.networkComponent.name }}</a-descriptions-item>
            <a-descriptions-item label="组件类型">{{
              $smartEnumPlugin.getDescByValue('COMPONENT_TYPE_ENUM', gatewayDetail.networkComponent.type)
            }}</a-descriptions-item>
            <a-descriptions-item label="组件状态">
              <a-tag :color="gatewayDetail.networkComponent.status === 1 ? 'green' : 'red'">{{
                gatewayDetail.networkComponent.status === 1 ? '启用' : '禁用'
              }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item v-for="item in configItems" :key="item.key" :label="item.label">
              <a-tag v-if="item.isBoolean" :color="item.value ? 'green' : 'red'">{{ item.value ? '是' : '否' }}</a-tag>
              <template v-else>{{ item.value }}</template>
            </a-descriptions-item>
            <a-descriptions-item label="组件描述">{{ gatewayDetail.networkComponent.description || '无' }}</a-descriptions-item>
          </a-descriptions>
        </template>
        <template v-else>
          <a-empty description="未关联网络组件" />
        </template>
      </a-card>

      <!-- 协议信息 -->
      <a-card size="small" title="协议信息">
        <template v-if="gatewayDetail.protocol">
          <a-descriptions size="small" :column="3" bordered>
            <a-descriptions-item label="协议名称">{{ gatewayDetail.protocol.name }}</a-descriptions-item>
            <a-descriptions-item label="协议版本">{{ gatewayDetail.protocol.version }}</a-descriptions-item>
            <a-descriptions-item label="协议描述">{{ gatewayDetail.protocol.description || '无' }}</a-descriptions-item>
          </a-descriptions>
        </template>
        <template v-else>
          <a-empty description="未关联协议" />
        </template>
      </a-card>
    </template>
  </div>
</template>

<script setup>
  import { computed } from 'vue';
  import { NETWORK_COMPONENT_CONFIG_LABEL_MAP } from '/@/constants/business/gateway/gateway-const';

  const props = defineProps({
    gatewayDetail: { type: Object, default: null },
  });

  const configItems = computed(() => {
    const nc = props.gatewayDetail?.networkComponent;
    if (!nc || !nc.configuration) return [];
    let cfg = {};
    try {
      cfg = typeof nc.configuration === 'string' ? JSON.parse(nc.configuration) : nc.configuration;
    } catch (e) {
      return [];
    }
    return Object.entries(cfg).map(([key, value]) => ({
      key,
      label: NETWORK_COMPONENT_CONFIG_LABEL_MAP[key] || key,
      value,
      isBoolean: key === 'tlsEnabled',
    }));
  });
</script>
