<template>
  <a-modal :title="'处理告警'" :open="visible" :confirmLoading="confirmLoading" @ok="onSubmit" @cancel="onClose" :maskClosable="false">
    <a-form ref="formRef" :model="form" :labelCol="{ span: 6 }" :wrapperCol="{ span: 16 }">
      <a-form-item label="告警ID" name="id" style="display: none">
        <a-input v-model:value="form.id" />
      </a-form-item>
      <a-form-item label="设备Key">
        <a-input :value="form.deviceKey" disabled />
      </a-form-item>
      <a-form-item label="告警描述">
        <a-textarea :value="form.description" disabled :rows="2" />
      </a-form-item>
      <a-form-item label="处理状态" name="status" :rules="[{ required: true, message: '请选择处理状态' }]">
        <SmartEnumSelect
          v-model:value="form.status"
          enum-name="ALARM_STATUS_ENUM"
          width="100%"
          placeholder="请选择处理状态"
          :exclude="[ALARM_STATUS_ENUM.PENDING.value]"
        />
      </a-form-item>
      <a-form-item label="处理备注" name="handleNote">
        <a-textarea v-model:value="form.handleNote" placeholder="请输入处理备注" :rows="3" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { reactive, ref, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { alarmLogApi } from '/@/api/business/alarmlog/alarm-log-api';
  import { ALARM_STATUS_ENUM } from '/@/constants/business/alarmlog/alarm-log-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';

  // 事件
  const emit = defineEmits(['reloadList']);

  // 是否显示
  const visible = ref(false);
  const confirmLoading = ref(false);

  // 表单
  const defaultForm = {
    id: null,
    deviceKey: '',
    description: '',
    status: undefined,
    handleNote: '',
  };
  const form = reactive({ ...defaultForm });

  function show(record) {
    Object.assign(form, {
      id: record.id,
      deviceKey: record.deviceKey,
      description: record.description,
      status: undefined,
      handleNote: '',
    });
    visible.value = true;
  }

  function onClose() {
    visible.value = false;
    nextTick(() => {
      Object.assign(form, defaultForm);
    });
  }

  async function onSubmit() {
    // 点击确定，验证表单
    if (!form.status) {
      message.warning('请选择处理状态');
      return;
    }
    confirmLoading.value = true;
    try {
      SmartLoading.show();
      // 处理API
      await alarmLogApi.handle({
        id: form.id,
        status: form.status,
        handleNote: form.handleNote,
      });
      message.success('处理成功');
      onClose();
      emit('reloadList');
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
      confirmLoading.value = false;
    }
  }

  defineExpose({ show });
</script>
