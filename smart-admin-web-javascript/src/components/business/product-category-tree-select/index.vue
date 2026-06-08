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

  // 选中值变化 → v-model 同步
  function onChange(val) {
    emit('update:value', val);
  }

  // 选中节点 → 抛出完整节点对象（含 name 等）
  function onSelect(val, node) {
    emit('select', node);
  }

  // 加载分类树供选择
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
