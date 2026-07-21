<template>
  <a-input v-model:value="val" :maxlength="props.valueType?.maxLength" :placeholder="placeholder" :disabled="disabled" />
</template>

<script setup>
  import { ref, watch, onMounted } from 'vue';
  import { validateValue } from './validate-value.js';
  const props = defineProps({ value: { default: '' }, valueType: { type: Object, required: true }, disabled: { type: Boolean, default: false } });
  const emit = defineEmits(['update:value']);
  const val = ref('');
  onMounted(() => {
    val.value = props.value;
  });
  watch(val, (v) => {
    emit('update:value', v);
  });
  const placeholder = props.valueType?.maxLength ? `最多${props.valueType.maxLength}个字符` : '请输入';

  function validate() {
    const e = validateValue(val.value, props.valueType);
    return e ? [e] : [];
  }
  defineExpose({ validate });
</script>
