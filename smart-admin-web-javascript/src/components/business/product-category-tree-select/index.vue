<template>
  <a-tree-select
    :value="value"
    style="width: 100%"
    :tree-data="treeData"
    :fieldNames="{ label: 'name', value: 'id', children: 'children' }"
    :placeholder="placeholder"
    allow-clear
    @update:value="onChange"
    @select="onSelect"
  />
</template>

<script setup>
  import { ref, onMounted } from 'vue';
  import { productCategoryApi } from '/@/api/business/productcategory/product-category-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const props = defineProps({
    value: [Number, String],
    placeholder: { type: String, default: '请选择产品分类' },
  });

  const emit = defineEmits(['update:value', 'select']);

  const treeData = ref([]);

  function onChange(val) {
    emit('update:value', val);
  }

  function onSelect(val, node) {
    emit('select', node);
  }

  async function loadTree() {
    try {
      const res = await productCategoryApi.queryTree();
      treeData.value = res.data || [];
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  onMounted(loadTree);

  defineExpose({ loadTree });
</script>
