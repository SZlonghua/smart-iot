<template>
  <a-select v-model:value="val" style="width: 100%" :disabled="disabled" placeholder="请选择">
    <a-select-option v-for="opt in options" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
  </a-select>
</template>

<script setup>
  import { ref, computed, watch, onMounted } from 'vue';
  import { validateValue } from './validate-value.js';

  const props = defineProps({ value: { default: null }, valueType: { type: Object, required: true }, disabled: { type: Boolean, default: false } });
  const emit = defineEmits(['update:value']);
  const val = ref('');
  onMounted(() => {
    val.value = props.value;
  });
  watch(val, (v) => {
    emit('update:value', v);
  });

  const options = computed(() => {
    const vt = props.valueType || {};
    return [
      { value: vt.trueValue || 'true', label: `${vt.trueText || '是'}(${vt.trueValue || 'true'})` },
      { value: vt.falseValue || 'false', label: `${vt.falseText || '否'}(${vt.falseValue || 'false'})` },
    ];
  });

  function validate() {
    const e = validateValue(val.value, props.valueType);
    return e ? [e] : [];
  }
  defineExpose({ validate });
</script>
