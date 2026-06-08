<!-- 数值类型编辑器：int/double/float/long 共用 -->
<template>
  <a-form-item label="最小值"><a-input-number v-model:value="value.min" style="width: 100%" :disabled="disabled" /></a-form-item>
  <a-form-item label="最大值"><a-input-number v-model:value="value.max" style="width: 100%" :disabled="disabled" /></a-form-item>
  <a-form-item label="单位">
    <a-select v-model:value="value.unit" placeholder="请选择单位" allow-clear show-search :disabled="disabled">
      <a-select-opt-group v-for="group in UNIT_GROUPS" :key="group.label" :label="group.label">
        <a-select-option v-for="u in group.options" :key="u.value" :value="u.value">{{ u.label }}</a-select-option>
      </a-select-opt-group>
    </a-select>
  </a-form-item>
</template>

<script setup>
  import { watch, onMounted } from 'vue';
  import { UNIT_GROUPS } from '/@/constants/business/product/unit-const';
  const props = defineProps({ value: { type: Object, required: true }, disabled: Boolean });
  onMounted(() => {
    if (props.value.min === undefined) props.value.min = null;
    if (props.value.max === undefined) props.value.max = null;
    if (props.value.unit === undefined) props.value.unit = '';
  });

  const EXTREMES = {
    int: { min: -2147483648, max: 2147483647 },
    long: { min: Number.MIN_SAFE_INTEGER, max: Number.MAX_SAFE_INTEGER },
    float: { min: -3.4028235e38, max: 3.4028235e38 },
    double: { min: -Number.MAX_VALUE, max: Number.MAX_VALUE },
  };

  function normalize(vt) {
    const d = EXTREMES[vt?.type];
    if (d) {
      if (vt.min == null || vt.min === '') vt.min = d.min;
      if (vt.max == null || vt.max === '') vt.max = d.max;
    }
  }
  function clean(vt) {
    const d = EXTREMES[vt?.type];
    if (d) {
      if (vt.min === d.min) vt.min = null;
      if (vt.max === d.max) vt.max = null;
    }
  }
  function serialize(vt) {
    const r = { type: vt.type, min: vt.min, max: vt.max };
    if (vt.unit) r.unit = vt.unit;
    return r;
  }

  const emit = defineEmits(['valueChange']);
  watch(
    () => props.value,
    () => {
      emit('valueChange');
    },
    { deep: true }
  );

  defineExpose({ validate: () => null, normalize, clean, serialize });
</script>
