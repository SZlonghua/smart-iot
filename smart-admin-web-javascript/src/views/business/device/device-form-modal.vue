<template>
  <a-drawer
    :title="form.id ? (readonly ? '查看' : '编辑') : '添加'"
    :width="500"
    :open="visible"
    :body-style="{ paddingBottom: '80px' }"
    @close="onClose"
  >
    <a-form ref="formRef" :model="form" :rules="readonly ? {} : rules" :label-col="{ span: 5 }">
      <a-form-item label="产品" name="productId">
        <ProductSelect v-model:value="form.productId" placeholder="请选择产品" @select="onProductSelect" :disabled="readonly" />
      </a-form-item>
      <a-form-item label="设备名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入设备名称" :disabled="readonly" />
      </a-form-item>
      <a-form-item label="描述" name="description">
        <a-textarea v-model:value="form.description" placeholder="请输入描述" :rows="3" :disabled="readonly" />
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
      <a-button style="margin-right: 8px" @click="onClose">关闭</a-button>
      <a-button v-if="!readonly" type="primary" @click="onSubmit">提交</a-button>
    </div>
  </a-drawer>
</template>

<script setup>
  import { ref, nextTick, reactive } from 'vue';
  import { message } from 'ant-design-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { deviceApi } from '/@/api/business/device/device-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import ProductSelect from '/@/components/business/product-select/index.vue';
  import _ from 'lodash';

  const emit = defineEmits(['reloadList']);
  const formRef = ref();

  const formDefault = {
    id: undefined,
    productId: undefined,
    productName: '',
    name: '',
    description: '',
  };
  let form = reactive({ ...formDefault });

  const rules = {
    productId: [{ required: true, message: '产品不能为空' }],
    name: [{ required: true, message: '设备名称不能为空' }],
  };

  const visible = ref(false);
  const readonly = ref(false);

  function showDrawer(rowData) {
    readonly.value = false;
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    visible.value = true;
    nextTick(() => {
      formRef.value.clearValidate();
    });
  }

  function onProductSelect(val, option) {
    form.productName = option.name || '';
  }

  function onClose() {
    Object.assign(form, formDefault);
    visible.value = false;
  }

  function onSubmit() {
    formRef.value
      .validate()
      .then(async () => {
        SmartLoading.show();
        try {
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
        message.error('参数验证错误，请仔细填写表单数据!');
      });
  }

  defineExpose({ showDrawer });
</script>
