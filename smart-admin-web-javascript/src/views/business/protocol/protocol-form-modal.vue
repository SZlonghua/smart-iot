<template>
  <a-drawer :title="form.id ? '编辑' : '添加'" :width="500" :open="visible" :body-style="{ paddingBottom: '80px' }" @close="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="协议名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入协议名称" />
      </a-form-item>
      <a-form-item label="版本号" name="version">
        <a-input v-model:value="form.version" placeholder="请输入版本号" />
      </a-form-item>
      <a-form-item label="JAR包路径" name="jarPath">
        <a-input v-model:value="form.jarPath" placeholder="请输入JAR包路径" />
      </a-form-item>
      <a-form-item label="协议描述" name="description">
        <a-input v-model:value="form.description" placeholder="请输入协议描述" />
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
  import { protocolApi } from '/@/api/business/protocol/protocol-api';
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
    version: '',
    jarPath: '',
    description: '',
  };
  let form = reactive({ ...formDefault });

  const rules = {
    name: [{ required: true, message: '协议名称不能为空' }],
    version: [{ required: true, message: '版本号不能为空' }],
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
            await protocolApi.update(form);
          } else {
            await protocolApi.add(form);
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
