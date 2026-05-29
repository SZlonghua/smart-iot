<template>
  <a-drawer :title="form.id ? '编辑' : '添加'" :width="500" :open="visible" :body-style="{ paddingBottom: '80px' }" @close="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="设备名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入设备名称" />
      </a-form-item>
      <a-form-item label="父设备ID" name="parentDeviceId">
        <a-input-number v-model:value="form.parentDeviceId" placeholder="请输入父设备ID" style="width: 100%" :min="0" />
      </a-form-item>
      <a-form-item label="产品ID" name="productId">
        <a-input-number v-model:value="form.productId" placeholder="请输入产品ID" style="width: 100%" :min="0" />
      </a-form-item>
      <a-form-item v-if="form.id" label="状态" name="status">
        <a-radio-group v-model:value="form.status">
          <a-radio :value="0">离线</a-radio>
          <a-radio :value="1">在线</a-radio>
          <a-radio :value="2">禁用</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
    <div
      :style="{
        position: 'absolute',
        right: 0,
        bottom: 0,
        width: '100%',
        borderTop: '1px solid #e9e9e9',
        padding: '10px 16px',
        background: '#fff',
        textAlign: 'right',
        zIndex: 1,
      }"
    >
      <a-button style="margin-right: 8px" @click="onClose">取消</a-button>
      <a-button type="primary" @click="onSubmit">提交</a-button>
    </div>
  </a-drawer>
</template>

<script setup>
  import { ref, nextTick, reactive } from 'vue';
  import { message } from 'ant-design-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { deviceApi } from '/@/api/business/device/device-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import _ from 'lodash';

  // 事件
  const emit = defineEmits(['reloadList']);
  // 组件ref
  const formRef = ref();

  // 表单
  const formDefault = {
    id: undefined,
    name: '',
    parentDeviceId: undefined,
    productId: undefined,
    status: undefined,
  };
  let form = reactive({ ...formDefault });

  const rules = {
    name: [{ required: true, message: '设备名称不能为空' }],
  };

  // 是否显示
  const visible = ref(false);

  function showDrawer(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    visible.value = true;
    nextTick(() => {
      formRef.value.clearValidate();
    });
  }

  function onClose() {
    Object.assign(form, formDefault);
    visible.value = false;
  }

  function onSubmit() {
    formRef.value
      .validate()
      .then(async () => {
        // 点击确定，验证表单
        SmartLoading.show();
        try {
          // 新建、修改API
          if (form.id) {
            await deviceApi.update(form);
          } else {
            await deviceApi.add(form);
          }
          message.success(`${form.id ? '修改' : '添加'}成功`);
          onClose();
          emit('reloadList');
        } catch (error) {
          smartSentry.captureError(error);
        } finally {
          SmartLoading.hide();
        }
      })
      .catch((error) => {
        console.log('error', error);
        message.error('参数验证错误，请仔细填写表单数据!');
      });
  }

  defineExpose({ showDrawer });
</script>
