<template>
  <a-input-number v-model:value="val" :min="min" :max="max" :step="step" style="width: 100%" :placeholder="`请输入${label}`" :disabled="disabled" />
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

  const step = computed(() => (props.valueType?.type === 'int' || props.valueType?.type === 'long' ? 1 : 0.01));
  const min = computed(() => props.valueType?.min ?? undefined);
  const max = computed(() => props.valueType?.max ?? undefined);
  const label = computed(() => {
    const t = props.valueType?.type;
    if (t === 'int') return '整数';
    if (t === 'long') return '长整数';
    if (t === 'float') return '单精度浮点';
    return '双精度浮点';
  });

  function validate() {
    if (val.value === null || val.value === undefined || val.value === '') return ['请输入值'];
    if (props.valueType?.min !== undefined && val.value < props.valueType.min) return [`最小值不能小于${props.valueType.min}`];
    if (props.valueType?.max !== undefined && val.value > props.valueType.max) return [`最大值不能大于${props.valueType.max}`];
    return [];
  }

  defineExpose({ validate });
</script>
