<template>
  <a-select v-model:value="val" style="width: 100%" :disabled="disabled" placeholder="请选择">
    <a-select-option v-for="el in options" :key="el.value" :value="el.value">{{ el.label }}</a-select-option>
  </a-select>
</template>

<script setup>
  import { ref, computed, watch, onMounted } from 'vue';

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
    return (props.valueType?.elements || []).map((el) => ({
      value: el.value,
      label: `${el.text || ''}(${el.value})`,
    }));
  });

  function validate() {
    if (val.value === null || val.value === undefined) return ['请选择值'];
    return [];
  }
  defineExpose({ validate });
</script>
