<template>
  <div>
    <!-- ====== 基础信息卡片 ====== -->
    <a-card size="small" :bordered="false" style="margin-bottom: 16px">
      <template #title>基础信息</template>
      <template #extra>
        <a-button type="link" @click="goBack"><ArrowLeftOutlined />返回</a-button>
      </template>
      <a-descriptions :column="3" size="small" bordered>
        <a-descriptions-item label="Device Key">
          {{ detail.deviceKey }}
          <a-button type="link" size="small" @click="copyText(detail.deviceKey)">复制</a-button>
        </a-descriptions-item>
        <a-descriptions-item label="设备名称">{{ detail.name }}</a-descriptions-item>
        <a-descriptions-item label="产品名称">{{ detail.productName }}</a-descriptions-item>
        <a-descriptions-item label="设备密钥">
          {{ secretVisible ? detail.deviceSecret : '**************************' }}
          <a-button type="link" size="small" @click="secretVisible = !secretVisible">{{ secretVisible ? '隐藏' : '查看' }}</a-button>
          <a-button type="link" size="small" @click="copyText(detail.deviceSecret)">复制</a-button>
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="statusColor(detail.status)">{{ $smartEnumPlugin.getDescByValue('DEVICE_STATUS_ENUM', detail.status) }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="父设备">{{ detail.parentDevice?.name || '-' }}</a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</a-descriptions-item>
        <a-descriptions-item label="最后上线时间">{{ detail.lastOnlineTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ detail.createTime }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <!-- ====== Tab页 ====== -->
    <a-card size="small" :bordered="false">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="data" tab="运行数据">
          <PropertyDataTab :deviceId="id" :properties="thingsProps" />
        </a-tab-pane>
        <a-tab-pane key="func" tab="功能调用">
          <FunctionCallTab :deviceId="id" :functions="thingsFuncs" />
        </a-tab-pane>
        <a-tab-pane key="log" tab="设备日志">
          <LogTab :deviceId="id" />
        </a-tab-pane>
        <a-tab-pane key="gateway" tab="在线接入设备网关">
          <GatewayTab :gatewayDetail="detail.gatewayDetail" />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { ArrowLeftOutlined } from '@ant-design/icons-vue';
  import { message } from 'ant-design-vue';
  import { useUserStore } from '/@/store/modules/system/user';
  import { deviceApi } from '/@/api/business/device/device-api';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { smartSentry } from '/@/lib/smart-sentry';
  import PropertyDataTab from './components/property-data-tab.vue';
  import FunctionCallTab from './components/function-call-tab.vue';
  import LogTab from './components/log-tab.vue';
  import GatewayTab from './components/gateway-tab.vue';

  const route = useRoute();
  const router = useRouter();
  const id = Number(route.query.id);

  const detail = ref({});
  const secretVisible = ref(false);
  const activeTab = ref('data');

  const thingsProps = computed(() => detail.value.productDetail?.thingsMetadata?.properties || []);
  const thingsFuncs = computed(() => detail.value.productDetail?.thingsMetadata?.functions || []);

  function statusColor(status) {
    const map = { 0: 'blue', 1: 'green', 2: 'red' };
    return map[status] || 'default';
  }

  function copyText(text) {
    if (!text) return;
    navigator.clipboard.writeText(text).then(() => message.success('已复制'));
  }

  function goBack() {
    useUserStore().closePage(route, router);
  }

  onMounted(async () => {
    try {
      SmartLoading.show();
      const res = await deviceApi.getDetail(id);
      detail.value = res.data || {};
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  });
</script>
