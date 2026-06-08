<!-- 数组类型编辑器：元素类型通过嵌套 ValueTypeEditor 选择 -->
<template>
  <a-form-item label="元素类型">
    <ValueTypeEditor
      ref="nestedRef"
      :value="value.elementType"
      :allowedTypes="basicTypes"
      :disabled="disabled"
      @update:value="(v) => (value.elementType = v)"
    />
  </a-form-item>
</template>

<script setup>
  import { ref, watch, defineAsyncComponent } from 'vue';
  const ValueTypeEditor = defineAsyncComponent(() => import('../index.vue'));
  const props = defineProps({ value: { type: Object, required: true }, disabled: Boolean });
  const basicTypes = ['int', 'double', 'float', 'long', 'boolean', 'string', 'enum', 'date'];
  const nestedRef = ref(null);
  watch(
    () => props.value.type,
    (t) => {
      if (t && !props.value.elementType) props.value.elementType = { type: 'string' };
    },
    { immediate: true }
  );

  function normalize() {
    nestedRef.value?.normalize?.();
  }
  function clean() {
    nestedRef.value?.clean?.();
  }
  function serialize(vt) {
    const r = { type: vt.type };
    if (vt.elementType) r.elementType = nestedRef.value?.serialize?.() || vt.elementType;
    return r;
  }

  defineExpose({ validate: () => null, normalize, clean, serialize });
</script>
