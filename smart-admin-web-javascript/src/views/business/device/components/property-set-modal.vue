<template>
  <a-modal
    :title="`设置属性: ${property?.name || ''}`"
    :open="visible"
    @ok="onConfirm"
    @cancel="onClose"
    :destroyOnClose="true"
    :confirmLoading="loading"
  >
    <a-form :label-col="{ span: 4 }">
      <a-form-item label="属性标识"><a-input :value="property?.id" disabled /></a-form-item>
      <a-form-item label="类型"><a-input :value="property?.valueType?.type" disabled /></a-form-item>
      <a-form-item label="当前值"><a-input :value="currentValue" disabled /></a-form-item>
      <a-form-item label="新值" :help="errMsg" :validate-status="errMsg ? 'error' : ''">
        <PropertyInput ref="inputRef" v-model:value="newValue" :valueType="property?.valueType" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { ref } from 'vue';
  import { message } from 'ant-design-vue';
  import { deviceApi } from '/@/api/business/device/device-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import PropertyInput from '/@/components/business/device/property-input.vue';

  const props = defineProps({ deviceId: { type: Number, required: true } });
  const emit = defineEmits(['updated']);

  const visible = ref(false);
  const loading = ref(false);
  const property = ref(null);
  const currentValue = ref('');
  const newValue = ref(null);
  const inputRef = ref();
  const errMsg = ref('');

  function open(prop, curVal) {
    property.value = prop;
    currentValue.value = curVal || '';
    newValue.value = null;
    errMsg.value = '';
    visible.value = true;
  }

  function onClose() {
    visible.value = false;
  }

  async function onConfirm() {
    const errs = inputRef.value?.validate?.() || [];
    if (errs.length) {
      errMsg.value = errs.join('; ');
      return;
    }
    loading.value = true;
    try {
      const res = await deviceApi.writeProperties(props.deviceId, { [property.value.id]: newValue.value });
      const data = res.data || {};
      if (data[property.value.id] !== undefined) {
        const v = data[property.value.id];
        const display = typeof v === 'object' ? JSON.stringify(v) : String(v ?? '');
        emit('updated', property.value.id, { formatValue: display, timeValue: new Date().toLocaleString() });
      }
      message.success('设置成功');
      onClose();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  }

  defineExpose({ open });
</script>
