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
  import { ref, watch, onMounted, defineAsyncComponent } from 'vue';
  const ValueTypeEditor = defineAsyncComponent(() => import('../index.vue'));
  const props = defineProps({ value: { type: Object, required: true }, disabled: Boolean });
  const basicTypes = ['int', 'double', 'float', 'long', 'boolean', 'string', 'enum', 'date'];
  const nestedRef = ref(null);
  onMounted(() => {
    if (!props.value.elementType) props.value.elementType = { type: 'string' };
  });

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

  const emit = defineEmits(['valueChange']);
  watch(
    () => props.value.elementType,
    () => emit('valueChange'),
    { deep: true }
  );

  defineExpose({ validate: () => null, normalize, clean, serialize });
</script>
