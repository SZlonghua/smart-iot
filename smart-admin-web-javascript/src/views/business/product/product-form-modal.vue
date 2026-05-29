<template>
  <a-drawer :title="form.id ? '编辑' : '添加'" :width="600" :open="visible" :body-style="{ paddingBottom: '80px' }" @close="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="产品名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入产品名称" />
      </a-form-item>
      <a-form-item label="产品分类" name="categoryId">
        <a-tree-select
          v-model:value="form.categoryId"
          style="width: 100%"
          :tree-data="categoryTree"
          :fieldNames="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="请选择产品分类"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="设备类型" name="deviceType">
        <SmartEnumSelect v-model:value="form.deviceType" enum-name="DEVICE_TYPE_ENUM" placeholder="请选择设备类型" width="100%" />
      </a-form-item>
      <a-form-item label="产品描述" name="description">
        <a-textarea v-model:value="form.description" placeholder="请输入产品描述" :rows="3" />
      </a-form-item>
      <a-form-item label="物模型" name="modelJson">
        <a-textarea v-model:value="form.modelJson" placeholder="请输入物模型JSON" :rows="8" />
      </a-form-item>
      <a-form-item v-if="form.id" label="状态" name="status">
        <a-radio-group v-model:value="form.status">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
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
  import { productApi } from '/@/api/business/product/product-api';
  import { productCategoryApi } from '/@/api/business/productcategory/product-category-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import _ from 'lodash';

  // 事件
  const emit = defineEmits(['reloadList']);
  // 组件ref
  const formRef = ref();

  // 表单
  const formDefault = {
    id: undefined,
    name: '',
    categoryId: undefined,
    categoryName: '',
    deviceType: 'direct',
    description: '',
    modelJson: '',
    status: 1,
  };
  let form = reactive({ ...formDefault });

  const rules = {
    name: [{ required: true, message: '产品名称不能为空' }],
    deviceType: [{ required: true, message: '设备类型不能为空' }],
  };

  // 是否显示
  const visible = ref(false);
  const categoryTree = ref([]);

  async function loadCategoryTree() {
    try {
      let res = await productCategoryApi.queryTree();
      categoryTree.value = res.data || [];
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  function showDrawer(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    loadCategoryTree();
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
          if (form.categoryId) {
            let cat = findCategory(categoryTree.value, form.categoryId);
            if (cat) {
              form.categoryName = cat.name;
            }
          }
          // 新建、修改API
          if (form.id) {
            await productApi.update(form);
          } else {
            await productApi.add(form);
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

  function findCategory(nodes, id) {
    for (let node of nodes) {
      if (node.id === id) return node;
      if (node.children) {
        let found = findCategory(node.children, id);
        if (found) return found;
      }
    }
    return null;
  }

  defineExpose({ showDrawer });
</script>
