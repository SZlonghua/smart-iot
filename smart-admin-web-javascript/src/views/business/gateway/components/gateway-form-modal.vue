<template>
  <a-modal
    :open="visible"
    :title="form.id ? '编辑' : '添加'"
    :width="600"
    forceRender
    ok-text="确认"
    cancel-text="取消"
    @ok="onSubmit"
    @cancel="onClose"
  >
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="网关名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入网关名称" />
      </a-form-item>

      <a-form-item label="网关类型" name="type">
        <a-input v-model:value="form.type" placeholder="请输入网关类型" />
      </a-form-item>

      <a-form-item label="网络组件ID" name="componentId">
        <a-input-number v-model:value="form.componentId" placeholder="网络组件ID" style="width: 100%" :min="1" />
      </a-form-item>

      <a-form-item label="协议ID" name="protocolId">
        <a-input-number v-model:value="form.protocolId" placeholder="协议ID" style="width: 100%" :min="1" />
      </a-form-item>

      <a-form-item label="传输方式" name="transport">
        <a-input v-model:value="form.transport" placeholder="传输方式(如tcp)" />
      </a-form-item>

      <a-form-item label="描述" name="description">
        <a-input v-model:value="form.description" placeholder="请输入描述" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import { message } from 'ant-design-vue';
  import { nextTick, reactive, ref } from 'vue';
  import { gatewayApi } from '/@/api/business/gateway/gateway-api';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { smartSentry } from '/@/lib/smart-sentry';

  defineExpose({
    showModal,
  });
  // 事件
  const emit = defineEmits(['refresh']);

  // --------------------- modal 显示与隐藏 ---------------------
  // 是否显示
  const visible = ref(false);

  function showModal(id) {
    Object.assign(form, formDefault);
    if (id) {
      form.id = id;
    }
    visible.value = true;
    nextTick(() => {
      const domArr = document.getElementsByClassName('ant-modal');
      if (domArr && domArr.length > 0) {
        Array.from(domArr).forEach((item) => {
          if (item.childNodes && item.childNodes.length > 0) {
            Array.from(item.childNodes).forEach((child) => {
              if (child.setAttribute) {
                child.setAttribute('aria-hidden', 'false');
              }
            });
          }
        });
      }
    });
  }

  function onClose() {
    visible.value = false;
  }

  // --------------------- 表单 ---------------------

  // 组件ref
  const formRef = ref();

  // 表单
  const formDefault = {
    id: undefined,
    name: undefined,
    type: undefined,
    componentId: undefined,
    protocolId: undefined,
    transport: undefined,
    description: undefined,
  };
  let form = reactive({ ...formDefault });
  const rules = {
    name: [{ required: true, message: '请输入网关名称' }],
    type: [{ required: true, message: '请输入网关类型' }],
  };

  function onSubmit() {
    formRef.value
      .validate()
      .then(async () => {
        // 点击确定，验证表单
        SmartLoading.show();
        try {
          // 新建、修改API
          if (form.id) {
            await gatewayApi.update(form);
          } else {
            await gatewayApi.add(form);
          }
          message.success(`${form.id ? '修改' : '添加'}成功`);
          emit('refresh');
          onClose();
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
</script>

<style lang="less" scoped>
  :deep(.ant-card-body) {
    padding: 10px;
  }
</style>
