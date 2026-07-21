<template>
  <a-row :gutter="12" style="height: 400px">
    <a-col :span="5" style="height: 100%">
      <div style="font-weight: bold; margin-bottom: 8px">功能列表</div>
      <a-menu v-model:selectedKeys="selectedKeys" mode="inline" style="border: 1px solid #f0f0f0; height: calc(100% - 30px); overflow-y: auto">
        <a-menu-item v-for="f in functions" :key="f.id">{{ f.name }}</a-menu-item>
      </a-menu>
    </a-col>
    <a-col :span="10" style="height: 100%; display: flex; flex-direction: column">
      <div style="font-weight: bold; margin-bottom: 8px">输入参数 (JSON)</div>
      <div style="flex: 1; min-height: 0"><JsonEditor v-model:value="advInput" /></div>
      <div style="position: sticky; bottom: 0; background: #fff; padding: 8px 0; text-align: right; border-top: 1px solid #f0f0f0">
        <a-button type="primary" @click="invoke" :disabled="!selectedFunc">执行</a-button>
        <a-button
          style="margin-left: 8px"
          @click="
            advInput = '';
            advResult = '';
          "
          >清空</a-button
        >
      </div>
    </a-col>
    <a-col :span="9" style="height: 100%; display: flex; flex-direction: column">
      <div style="font-weight: bold; margin-bottom: 8px">执行结果</div>
      <div style="flex: 1; min-height: 0"><JsonEditor :value="advResult" disabled /></div>
    </a-col>
  </a-row>
</template>

<script setup>
  import { ref, computed, watch, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { deviceApi } from '/@/api/business/device/device-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { sampleValue } from '/@/components/business/device/sample-value.js';
  import JsonEditor from '/@/components/business/device/json-editor.vue';

  const props = defineProps({ deviceId: { type: Number, required: true }, functions: { type: Array, default: () => [] } });

  const selectedKeys = ref([]);
  const advInput = ref('');
  const advResult = ref('');

  const selectedFunc = computed(() => {
    if (selectedKeys.value.length === 0) return null;
    return props.functions.find((f) => f.id === selectedKeys.value[0]) || null;
  });

  onMounted(() => {
    if (props.functions?.length) selectedKeys.value = [props.functions[0].id];
  });
  // 初始化 + 切换功能 → 生成 JSON 示例
  watch(
    selectedKeys,
    () => {
      advResult.value = '';
      const func = selectedFunc.value;
      if (!func || !func.inputs?.length) {
        advInput.value = '{}';
        return;
      }
      const obj = {};
      func.inputs.forEach((inp) => {
        obj[inp.id] = sampleValue(inp.valueType);
      });
      advInput.value = JSON.stringify(obj, null, 2);
    },
    { immediate: true }
  );

  function prettyJson(v) {
    try {
      return JSON.stringify(v, null, 2);
    } catch {
      return String(v);
    }
  }

  function isNull(v) {
    return v === undefined || v === null;
  }
  async function invoke() {
    if (!selectedFunc.value) return;
    let params;
    try {
      params = JSON.parse(advInput.value || '{}');
    } catch {
      message.warning('JSON格式错误');
      return;
    }
    const inputs = selectedFunc.value.inputs || [];
    const missing = inputs.filter((inp) => inp.required && isNull(params[inp.id])).map((inp) => inp.id || inp.name);
    if (missing.length) {
      message.warning('以下必填参数未填写: ' + missing.join(', '));
      return;
    }
    try {
      const filtered = {};
      Object.keys(params).forEach((k) => {
        const v = params[k];
        if (v !== null && v !== undefined) filtered[k] = v;
      });
      const res = await deviceApi.invokeFunction(props.deviceId, selectedFunc.value.id, filtered);
      advResult.value = prettyJson(res.data);
      message.success('指令已下发');
    } catch (e) {
      advResult.value = prettyJson(e);
      smartSentry.captureError(e);
    }
  }
</script>
