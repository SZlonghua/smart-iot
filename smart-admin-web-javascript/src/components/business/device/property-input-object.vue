<template>
  <div>
    <a-textarea v-model:value="val" :rows="8" :disabled="disabled" />
    <div v-if="err" style="color: #ff4d4f; font-size: 12px; margin-top: 4px">{{ err }}</div>
  </div>
</template>

<script setup>
  import { ref, computed, watch, onMounted } from 'vue';
  import { validateValue } from './validate-value.js';
  import { sampleValue } from './sample-value.js';

  const props = defineProps({ value: { default: '' }, valueType: { type: Object, required: true }, disabled: { type: Boolean, default: false } });
  const emit = defineEmits(['update:value']);
  const val = ref('');
  const err = ref('');

  const sample = computed(() => {
    const props2 = props.valueType?.properties || [];
    if (!props2.length) return '请输入JSON对象，如: {}';
    const obj = {};
    props2.forEach((prop) => { obj[prop.id] = sampleValue(prop.valueType); });
    return JSON.stringify(obj, null, 2);
  });

  onMounted(() => {
    val.value = props.value || sample.value;
  });

  watch(val, (v) => {
    if (!v) {
      emit('update:value', null);
      err.value = '';
      return;
    }
    try {
      const obj = JSON.parse(v);
      emit('update:value', typeof obj === 'object' && !Array.isArray(obj) ? obj : v);
    } catch {
      emit('update:value', v);
    }
    err.value = '';
  });

  function validate() {
    const e = validateValue(val.value, props.valueType);
    return e ? [e] : [];
  }

  defineExpose({ validate });
</script>
