<template>
  <div>
    <a-input-search v-model:value="search" placeholder="请输入属性名" style="width: 320px; margin-bottom: 16px" />
    <a-row v-if="filteredList.length" :gutter="[12, 12]">
      <a-col :span="6" v-for="prop in filteredList" :key="prop.id">
        <a-card size="small" hoverable class="prop-card">
          <template #title>{{ prop.name }}</template>
          <template #extra>
            <a-tag :color="am(prop.accessMode).color" size="small">{{ am(prop.accessMode).label }}</a-tag>
          </template>
          <div style="margin-top: 4px; font-size: 12px; color: #8c8c8c">属性标识: {{ prop.id }}</div>
          <a-statistic :value="propValues[prop.id]?.formatValue || '--'" :value-style="{ color: '#1677ff', fontSize: '24px', fontWeight: 700 }" />

          <div style="font-size: 12px; color: #bfbfbf">更新时间: {{ propValues[prop.id]?.timeValue || '-' }}</div>
          <template #actions>
            <a-button size="small" type="link" :disabled="!am(prop.accessMode).canWrite" @click="openSetModal(prop)">设置</a-button>
            <a-button size="small" type="link" :disabled="!am(prop.accessMode).canRead" @click="handleGet(prop)">获取最新</a-button>
          </template>
        </a-card>
      </a-col>
    </a-row>
    <a-empty v-else description="暂无属性数据" />
    <PropertySetModal ref="setModalRef" :deviceId="props.deviceId" @updated="onPropertyUpdated" />
  </div>
</template>

<script setup>
  import { ref, reactive, computed, watch, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { deviceApi } from '/@/api/business/device/device-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { accessMode } from '/@/constants/business/access-mode-const';
  import PropertySetModal from './property-set-modal.vue';

  const props = defineProps({ deviceId: { type: Number, required: true }, properties: { type: Array, default: () => [] } });
  const search = ref('');
  const propValues = reactive({});
  const setModalRef = ref();

  const filteredList = computed(() => {
    if (!search.value) return props.properties;
    return props.properties.filter((p) => p.id.includes(search.value) || p.name.includes(search.value));
  });

  async function loadProperties() {
    if (!filteredList.value.length) return;
    try {
      const ids = filteredList.value.map((p) => p.id);
      const res = await deviceApi.getProperties(props.deviceId, ids);
      const list = res.data || [];
      list.forEach((item) => {
        propValues[item.propertyId] = { formatValue: item.formatValue, timeValue: item.timeValue };
      });
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  onMounted(loadProperties);
  watch(filteredList, loadProperties);

  function am(m) {
    return accessMode(m);
  }

  function openSetModal(prop) {
    const cur = propValues[prop.id];
    setModalRef.value?.open(prop, cur?.formatValue);
  }
  function onPropertyUpdated(propId, val) {
    propValues[propId] = val;
  }
  async function handleGet(prop) {
    try {
      const res = await deviceApi.readProperties(props.deviceId, [prop.id]);
      if (res.data && res.data[prop.id] !== undefined) {
        propValues[prop.id] = { formatValue: res.data[prop.id], timeValue: new Date().toLocaleString() };
      }
      message.success('获取成功');
    } catch (e) {
      smartSentry.captureError(e);
    }
  }
</script>
<style scoped>
  .prop-card :deep(.ant-card-head) {
    background: #fafafa;
  }
</style>
