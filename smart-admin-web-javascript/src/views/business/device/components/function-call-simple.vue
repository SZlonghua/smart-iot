<template>
  <a-row :gutter="12" style="height: 400px">
    <a-col :span="5" style="height: 100%">
      <div style="font-weight: bold; margin-bottom: 8px">功能列表</div>
      <a-menu v-model:selectedKeys="selectedKeys" mode="inline" style="border: 1px solid #f0f0f0; height: calc(100% - 30px); overflow-y: auto">
        <a-menu-item v-for="f in functions" :key="f.id">{{ f.name }}</a-menu-item>
      </a-menu>
    </a-col>
    <a-col :span="10" style="height: 100%; display: flex; flex-direction: column">
      <div style="font-weight: bold; margin-bottom: 8px">输入参数</div>
      <div v-if="selectedFunc" style="flex: 1; overflow-y: auto; border: 1px solid #f0f0f0; border-radius: 4px; padding: 8px">
        <a-table size="small" :columns="paramColumns" :dataSource="selectedFunc.inputs || []" :pagination="false" bordered>
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.dataIndex === 'required'">{{ record.required ? '是' : '否' }}</template>
            <template v-if="column.dataIndex === 'type'">{{ record.valueType?.type || '-' }}</template>
            <template v-if="column.dataIndex === 'value'">
              <PropertyInput :key="clearKey + '_' + index" v-model:value="simpleInputs[index]" :valueType="record.valueType" style="width: 100%" />
            </template>
          </template>
        </a-table>
      </div>
      <a-empty v-else description="请选择功能" style="flex: 1" />
      <div style="position: sticky; bottom: 0; background: #fff; padding: 8px 0; text-align: right; border-top: 1px solid #f0f0f0">
        <a-button type="primary" @click="invoke" :disabled="!selectedFunc">执行</a-button>
        <a-button style="margin-left: 8px" @click="clearSimple">清空</a-button>
      </div>
    </a-col>
    <a-col :span="9" style="height: 100%; display: flex; flex-direction: column">
      <div style="font-weight: bold; margin-bottom: 8px">执行结果</div>
      <div style="flex: 1; min-height: 0"><JsonEditor :value="simpleResult" disabled /></div>
    </a-col>
  </a-row>
</template>

<script setup>
  import { ref, computed, watch, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import { deviceApi } from '/@/api/business/device/device-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import PropertyInput from '/@/components/business/device/property-input.vue';
  import JsonEditor from '/@/components/business/device/json-editor.vue';

  const props = defineProps({ deviceId: { type: Number, required: true }, functions: { type: Array, default: () => [] } });

  const selectedKeys = ref([]);
  const simpleInputs = ref([]);
  const simpleResult = ref('');

  const paramColumns = [
    { title: '参数标识', dataIndex: 'id', width: 100 },
    { title: '参数名称', dataIndex: 'name', width: 100 },
    { title: '输入类型', dataIndex: 'type', width: 80 },
    { title: '必填', dataIndex: 'required', width: 60 },
    { title: '值', dataIndex: 'value', width: 150 },
  ];

  const selectedFunc = computed(() => {
    if (selectedKeys.value.length === 0) return null;
    return props.functions.find((f) => f.id === selectedKeys.value[0]) || null;
  });

  onMounted(() => {
    if (props.functions?.length) selectedKeys.value = [props.functions[0].id];
  });
  // 切换功能 → 清空上一次的输入值
  watch(selectedKeys, () => {
    simpleInputs.value = [];
    simpleResult.value = '';
  });

  function prettyJson(v) {
    try {
      return JSON.stringify(v, null, 2);
    } catch {
      return String(v);
    }
  }
  const clearKey = ref(0);
  function clearSimple() {
    simpleInputs.value = [];
    simpleResult.value = '';
    clearKey.value++;
  }

  function isNull(v) { return v === undefined || v === null; }
  async function invoke() {
    if (!selectedFunc.value) return;
    const inputs = selectedFunc.value.inputs || [];

    // 检查必填参数
    const missing = inputs.filter((inp, i) => inp.required && isNull(simpleInputs.value[i])).map((inp) => inp.id || inp.name);
    if (missing.length) {
      message.warning('以下必填参数未填写: ' + missing.join(', '));
      return;
    }

    try {
      const params = {};
      inputs.forEach((inp, i) => {
        const v = simpleInputs.value[i];
        if (v !== null && v !== undefined) params[inp.id] = v;
      });
      const res = await deviceApi.invokeFunction(props.deviceId, selectedFunc.value.id, params);
      simpleResult.value = prettyJson(res.data);
      message.success('指令已下发');
    } catch (e) {
      simpleResult.value = prettyJson(e);
      smartSentry.captureError(e);
    }
  }
</script>
