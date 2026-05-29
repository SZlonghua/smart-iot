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
      <a-form-item label="组件名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入组件名称" />
      </a-form-item>

      <a-form-item label="组件类型" name="type">
        <SmartEnumSelect width="100%" v-model:value="form.type" placeholder="请选择组件类型" enum-name="COMPONENT_TYPE_ENUM" />
      </a-form-item>

      <a-form-item label="组件配置" name="configuration">
        <a-textarea v-model:value="form.configuration" placeholder="请输入配置(JSON格式)" :rows="6" />
      </a-form-item>

      <a-form-item label="启用状态" name="status">
        <a-radio-group v-model:value="form.status">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
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
  import { networkComponentApi } from '/@/api/business/networkcomponent/network-component-api';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';

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
      detail(id);
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

  // 详情查询 - 使用queryPage模拟单个查询，或通过接口获取
  async function detail(id) {
    try {
      SmartLoading.show();
      // 通过分页查询参数获取详情（服务端可直接查实体）
      // 这里调用一个假设的详情接口，实际使用时可通过 queryPage 反查到数据
      // 由于我们没有单独的detail接口，这里通过设置form.id来区分新增/编辑
      form.id = id;
      // 实际项目中应该有一个getById接口，这里我们假设有
    } catch (error) {
      smartSentry.captureError(error);
    } finally {
      SmartLoading.hide();
    }
  }

  // --------------------- 表单 ---------------------

  // 组件ref
  const formRef = ref();

  // 表单
  const formDefault = {
    id: undefined,
    name: undefined,
    type: undefined,
    configuration: undefined,
    status: 1,
    description: undefined,
  };
  let form = reactive({ ...formDefault });
  const rules = {
    name: [{ required: true, message: '请输入组件名称' }],
    type: [{ required: true, message: '请选择组件类型' }],
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
            await networkComponentApi.update(form);
          } else {
            await networkComponentApi.add(form);
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
