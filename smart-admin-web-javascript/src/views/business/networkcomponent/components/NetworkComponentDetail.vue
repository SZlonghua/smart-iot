<template>
  <a-drawer v-if="record" :open="visible" title="查看网络组件" :width="700" placement="right" @close="onClose">
    <a-descriptions :column="2" bordered size="small" :label-style="{ width: '100px' }">
      <a-descriptions-item label="组件名称">{{ record.name }}</a-descriptions-item>
      <a-descriptions-item label="组件类型">{{ $smartEnumPlugin.getDescByValue('COMPONENT_TYPE_ENUM', record.type) }}</a-descriptions-item>
      <a-descriptions-item label="启用状态">
        <a-tag :color="record.status === 1 ? 'green' : 'red'">{{ record.status === 1 ? '启用' : '禁用' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="创建时间">{{ record.createTime }}</a-descriptions-item>
    </a-descriptions>

    <a-divider orientation="left" style="font-size: 13px; margin: 16px 0 8px">配置信息</a-divider>
    <a-descriptions :column="2" bordered size="small" :label-style="{ width: '120px' }">
      <template v-for="(value, key) in parsedConfig" :key="key">
        <a-descriptions-item :label="getConfigLabel(key)">
          <template v-if="key === 'tlsEnabled'">
            <a-tag :color="value ? 'green' : 'red'">{{ value ? '是' : '否' }}</a-tag>
          </template>
          <template v-else>{{ value }}</template>
        </a-descriptions-item>
      </template>
    </a-descriptions>

    <a-divider orientation="left" style="font-size: 13px; margin: 16px 0 8px">描述</a-divider>
    <div class="detail-description" v-html="record.description || '无'"></div>
  </a-drawer>
</template>

<script setup>
  import { ref, computed } from 'vue';

  const visible = ref(false);
  const record = ref(null);

  const configLabelMap = {
    localAddress: '本地地址',
    localPort: '本地端口',
    publicAddress: '公网地址',
    publicPort: '公网端口',
    remoteAddress: '远程地址',
    remotePort: '远程端口',
    clientId: 'Client ID',
    username: '用户名',
    password: '密码',
    maxMessageSize: '最大消息长度',
    tlsEnabled: '开启TLS',
  };

  const parsedConfig = computed(() => {
    if (!record.value || !record.value.configuration) {
      return {};
    }
    try {
      return typeof record.value.configuration === 'string'
        ? JSON.parse(record.value.configuration)
        : record.value.configuration;
    } catch (e) {
      return {};
    }
  });

  function getConfigLabel(key) {
    return configLabelMap[key] || key;
  }

  function show(data) {
    record.value = data;
    visible.value = true;
  }

  function onClose() {
    visible.value = false;
    record.value = null;
  }

  defineExpose({ show });
</script>

<style lang="less" scoped>
  .detail-description {
    min-height: 40px;
    padding: 12px;
    background: #fafafa;
    border: 1px solid #e8e8e8;
    border-radius: 4px;
    :deep(img) {
      max-width: 100%;
    }
  }
</style>
