<template>
  <a-drawer :title="form.id ? '编辑' : '添加'" :width="600" :open="visible" :body-style="{ paddingBottom: '80px' }" @close="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="产品名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入产品名称" />
      </a-form-item>
      <a-form-item label="产品分类" name="categoryId">
        <ProductCategoryTreeSelect ref="treeSelectRef" v-model:value="form.categoryId" placeholder="请选择产品分类" @select="onCategorySelect" />
      </a-form-item>
      <a-form-item label="设备类型" name="deviceType">
        <div class="device-type-cards">
          <div
            v-for="dt in deviceTypes"
            :key="dt.value"
            :class="['device-type-card', { active: form.deviceType === dt.value }]"
            @click="form.deviceType = dt.value"
          >
            <div class="card-icon">{{ dt.icon }}</div>
            <div class="card-label">{{ dt.label }}</div>
            <div class="card-desc">{{ dt.desc }}</div>
          </div>
        </div>
      </a-form-item>
      <a-form-item label="产品描述" name="description">
        <a-textarea v-model:value="form.description" placeholder="请输入产品描述" :rows="3" />
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
  import { productApi } from '/@/api/business/product/product-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import ProductCategoryTreeSelect from '/@/components/business/product-category-tree-select/index.vue';
  import _ from 'lodash';

  const emit = defineEmits(['reloadList']);
  const formRef = ref();

  // 设备类型卡片选项
  const deviceTypes = [
    { value: 'direct', label: '直连设备', icon: '📡', desc: '直接连接平台，支持MQTT协议' },
    { value: 'gateway', label: '网关设备', icon: '🌐', desc: '管理子设备，数据汇聚上报' },
    { value: 'gateway_child', label: '网关子设备', icon: '🔌', desc: '通过网关连接平台' },
  ];

  const formDefault = {
    id: undefined,
    name: '',
    categoryId: undefined,
    categoryName: '',
    deviceType: 'direct',
    description: '',
    status: 0,
  };
  let form = reactive({ ...formDefault });

  const rules = {
    name: [{ required: true, message: '产品名称不能为空' }],
    deviceType: [{ required: true, message: '设备类型不能为空' }],
  };

  const visible = ref(false);
  const treeSelectRef = ref();

  function onCategorySelect(node) {
    if (node) {
      form.categoryName = node.name;
    }
  }

  function showDrawer(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    treeSelectRef.value?.loadTree();
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
        SmartLoading.show();
        try {
          let submitData = { ...form };
          if (submitData.id) {
            await productApi.update(submitData);
          } else {
            await productApi.add(submitData);
          }
          message.success(`${submitData.id ? '修改' : '添加'}成功`);
          onClose();
          emit('reloadList');
        } catch (error) {
          smartSentry.captureError(error);
        } finally {
          SmartLoading.hide();
        }
      })
      .catch(() => {
        message.error('参数验证错误，请仔细填写表单数据!');
      });
  }

  defineExpose({ showDrawer });
</script>

<style scoped>
  .device-type-cards {
    display: flex;
    gap: 12px;
    width: 100%;
  }
  .device-type-card {
    flex: 1;
    padding: 16px 12px;
    border: 2px solid #e8e8e8;
    border-radius: 8px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
  }
  .device-type-card:hover {
    border-color: #91caff;
  }
  .device-type-card.active {
    border-color: #1677ff;
    background: #f0f5ff;
  }
  .card-icon {
    font-size: 28px;
    margin-bottom: 8px;
  }
  .card-label {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 4px;
  }
  .card-desc {
    font-size: 12px;
    color: #999;
  }
</style>
