<template>
  <div>
    <!-- ====== 基础信息卡片 ====== -->
    <a-card size="small" :bordered="false" style="margin-bottom: 16px">
      <template #title>基础信息</template>
      <template #extra
        ><a-button type="link" @click="goBack"><ArrowLeftOutlined />返回</a-button></template
      >
      <a-descriptions :column="3" size="small" bordered>
        <a-descriptions-item label="产品Key">
          {{ product.productKey }}
          <a-button type="link" size="small" @click="copyText(product.productKey)">复制</a-button>
        </a-descriptions-item>
        <a-descriptions-item label="产品名称">{{ product.name }}</a-descriptions-item>
        <a-descriptions-item label="产品分类">{{ product.categoryName }}</a-descriptions-item>
        <!-- 产品密钥：默认掩码，可切换查看 -->
        <a-descriptions-item label="产品密钥">
          {{ secretVisible ? product.productSecret : '**************************' }}
          <a-button type="link" size="small" @click="secretVisible = !secretVisible">{{ secretVisible ? '隐藏' : '查看' }}</a-button>
          <a-button type="link" size="small" @click="copyText(product.productSecret)">复制</a-button>
        </a-descriptions-item>
        <a-descriptions-item label="设备类型">{{ $smartEnumPlugin.getDescByValue('DEVICE_TYPE_ENUM', product.deviceType) }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="product.status === 1 ? 'green' : 'red'">{{ product.status === 1 ? '启用' : '禁用' }}</a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="描述" :span="2">{{ product.description || '-' }}</a-descriptions-item>
        <a-descriptions-item label="创建时间">{{ product.createTime }}</a-descriptions-item>
      </a-descriptions>
    </a-card>

    <!-- ====== Tab页：物模型 + Topic类列表 ====== -->
    <a-card size="small" :bordered="false">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="thingModel" tab="物模型">
          <!-- TSL 视图：JSON Viewer -->
          <template v-if="tslVisible">
            <a-row style="margin-bottom: 8px">
              <a-button type="primary" ghost @click="tslVisible = false">关闭TSL</a-button>
            </a-row>
            <JsonViewer :value="modelJson" :expanded="true" :expandDepth="10" copyable boxed theme="light" />
          </template>
          <!-- 编辑视图：工具栏 + 属性/功能/事件三个子Tab -->
          <template v-else>
            <a-row justify="space-between" align="middle" style="margin-bottom: 8px">
              <a-space>
                <a-button type="primary" ghost @click="tslVisible = true">物模型TSL</a-button>
                <a-button type="primary" ghost @click="handleImport">导入</a-button>
                <a-button type="primary" ghost @click="handleExport">导出</a-button>
              </a-space>
              <a-button
                type="primary"
                :loading="saving"
                :disabled="product.status === 1"
                :title="product.status === 1 ? '请先禁用产品再编辑物模型' : ''"
                @click="saveModel"
                >保存物模型</a-button
              >
            </a-row>
            <a-tabs v-model:activeKey="modelTab" size="small">
              <a-tab-pane key="properties" tab="属性定义"><PropertyTab v-model="modelJson.properties" /></a-tab-pane>
              <a-tab-pane key="functions" tab="功能定义"><FunctionTab v-model="modelJson.functions" /></a-tab-pane>
              <a-tab-pane key="events" tab="事件定义"><EventTab v-model="modelJson.events" /></a-tab-pane>
            </a-tabs>
          </template>
        </a-tab-pane>
        <!-- Topic 列表：硬编码 MQTT Topic -->
        <a-tab-pane key="topics" tab="Topic类列表">
          <TopicTab :productKey="product.productKey" />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';
  import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router';
  import { ArrowLeftOutlined } from '@ant-design/icons-vue';
  import { message, Modal } from 'ant-design-vue';
  import { useUserStore } from '/@/store/modules/system/user';
  import { productApi } from '/@/api/business/product/product-api';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { smartSentry } from '/@/lib/smart-sentry';
  import PropertyTab from './components/property-tab.vue';
  import FunctionTab from './components/function-tab.vue';
  import EventTab from './components/event-tab.vue';
  import TopicTab from './components/topic-tab.vue';

  const route = useRoute();
  const router = useRouter();
  const product = ref({});
  const modelJson = ref({ properties: [], functions: [], events: [] });
  const originalJson = ref(''); // 原始 JSON，用于检测是否有未保存改动

  const isDirty = computed(() => JSON.stringify(modelJson.value) !== originalJson.value);
  const activeTab = ref('thingModel');
  const modelTab = ref('properties');
  const secretVisible = ref(false); // 密钥可见性
  const tslVisible = ref(false); // TSL JSON 视图切换
  const saving = ref(false);

  // 格式化 JSON 字符串， 用于导出 JSON 文件
  const tslJson = computed(() => JSON.stringify(modelJson.value, null, 2));

  // ---- 工具函数 ----

  /** 复制文本到剪贴板 */
  function copyText(text) {
    navigator.clipboard.writeText(text).then(() => message.success('已复制'));
  }

  /** 导出物模型 JSON 文件 */
  function handleExport() {
    const blob = new Blob([tslJson.value], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${product.value.productKey || 'tsl'}.json`;
    a.click();
    URL.revokeObjectURL(url);
    message.success('导出成功');
  }

  /** 导入 JSON 文件替换物模型 */
  function handleImport() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = (e) => {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = (ev) => {
        try {
          const data = JSON.parse(ev.target.result);
          modelJson.value = { properties: data.properties || [], functions: data.functions || [], events: data.events || [] };
          message.success('导入成功');
        } catch (err) {
          message.error('JSON格式错误');
        }
      };
      reader.readAsText(file);
    };
    input.click();
  }

  // ---- 数据加载 ----

  onMounted(async () => {
    try {
      SmartLoading.show();
      const res = await productApi.getById(route.query.id);
      product.value = res.data;
      // 解析物模型 JSON，规范化结构
      if (res.data.modelJson) {
        try {
          const parsed = JSON.parse(res.data.modelJson);
          modelJson.value = { properties: parsed.properties || [], functions: parsed.functions || [], events: parsed.events || [] };
          originalJson.value = JSON.stringify(modelJson.value);
        } catch (e) {
          /* JSON 解析失败则使用默认空结构 */
        }
      }
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  });

  // ---- 保存 ----

  /** 保存物模型到后端（独立接口，不校验 name/deviceType） */
  async function saveModel() {
    saving.value = true;
    try {
      await productApi.saveModel({ id: product.value.id, modelJson: JSON.stringify(modelJson.value) });
      originalJson.value = JSON.stringify(modelJson.value); // 同步原始值，消除 dirty 状态
      message.success('物模型保存成功');
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      saving.value = false;
    }
  }

  // ---- 返回 ----

  /** 关闭当前标签页回到上一页 */
  function doClose() {
    useUserStore().closePage(route, router);
  }

  /** 返回按钮：有未保存改动则弹窗确认 */
  function goBack() {
    if (isDirty.value) {
      Modal.confirm({
        title: '提示',
        content: '物模型有未保存的改动，是否保存后离开？',
        okText: '保存并离开',
        cancelText: '不保存',
        onOk: async () => {
          await saveModel();
          doClose();
        },
        onCancel: () => doClose(),
      });
    } else {
      doClose();
    }
  }

  // 路由守卫：浏览器后退/跳转时拦截未保存改动
  onBeforeRouteLeave((to, from, next) => {
    if (!isDirty.value) return next();
    Modal.confirm({
      title: '提示',
      content: '物模型有未保存的改动，是否保存后离开？',
      okText: '保存并离开',
      cancelText: '不保存',
      onOk: async () => {
        await saveModel();
        next();
      },
      onCancel: () => next(),
    });
  });
</script>
