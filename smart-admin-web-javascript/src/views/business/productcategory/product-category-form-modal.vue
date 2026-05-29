<template>
  <a-drawer :title="form.id ? '编辑' : '添加'" :width="500" :open="visible" :body-style="{ paddingBottom: '80px' }" @close="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="分类名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入分类名称" />
      </a-form-item>
      <a-form-item label="父分类" name="parentId">
        <a-tree-select
          v-model:value="form.parentId"
          style="width: 100%"
          :tree-data="treeData"
          :fieldNames="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="请选择父分类（留空为顶级）"
          allow-clear
          :replaceFields="{ title: 'name', value: 'id', children: 'children' }"
        />
      </a-form-item>
      <a-form-item label="排序值" name="sortOrder">
        <a-input-number style="width: 100%" v-model:value="form.sortOrder" placeholder="请输入排序值" :min="0" />
      </a-form-item>
      <a-form-item label="描述" name="description">
        <a-input v-model:value="form.description" placeholder="请输入描述" />
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
  import { productCategoryApi } from '/@/api/business/productcategory/product-category-api';
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
    parentId: undefined,
    sortOrder: 0,
    description: '',
  };
  let form = reactive({ ...formDefault });

  const rules = {
    name: [{ required: true, message: '分类名称不能为空' }],
  };

  // 是否显示
  const visible = ref(false);
  const treeData = ref([]);

  async function loadTree() {
    try {
      let res = await productCategoryApi.queryTree();
      treeData.value = res.data || [];
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  function showDrawer(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    if (form.parentId === 0) {
      form.parentId = undefined;
    }
    loadTree();
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
          let submitData = { ...form };
          if (submitData.parentId === undefined || submitData.parentId === null) {
            submitData.parentId = 0;
          }
          // 新建、修改API
          if (submitData.id) {
            await productCategoryApi.update(submitData);
          } else {
            await productCategoryApi.add(submitData);
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
      .catch((error) => {
        console.log('error', error);
        message.error('参数验证错误，请仔细填写表单数据!');
      });
  }

  defineExpose({ showDrawer });
</script>
