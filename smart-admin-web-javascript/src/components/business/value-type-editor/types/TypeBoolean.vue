<!-- 布尔类型编辑器 -->
<template>
  <a-form-item label="true文本" required><a-input v-model:value="value.trueText" placeholder="如：开启" :disabled="disabled" /></a-form-item>
  <a-form-item label="false文本" required><a-input v-model:value="value.falseText" placeholder="如：关闭" :disabled="disabled" /></a-form-item>
  <a-form-item label="true值" required><a-input v-model:value="value.trueValue" placeholder="如：1" :disabled="disabled" /></a-form-item>
  <a-form-item label="false值" required><a-input v-model:value="value.falseValue" placeholder="如：0" :disabled="disabled" /></a-form-item>
</template>

<script setup>
  import { watch } from 'vue';
  const props = defineProps({ value: { type: Object, required: true }, disabled: Boolean });
  watch(
    () => props.value.type,
    (t) => {
      if (!t) return;
      if (props.value.trueText === undefined) props.value.trueText = '是';
      if (props.value.falseText === undefined) props.value.falseText = '否';
      if (props.value.trueValue === undefined) props.value.trueValue = 'true';
      if (props.value.falseValue === undefined) props.value.falseValue = 'false';
    },
    { immediate: true }
  );
  function validate() {
    const err = [];
    if (!props.value.trueText) err.push('true文本不能为空');
    if (!props.value.falseText) err.push('false文本不能为空');
    if (!props.value.trueValue) err.push('true值不能为空');
    if (!props.value.falseValue) err.push('false值不能为空');
    return err.length ? err : null;
  }
  function serialize(vt) {
    return {
      type: vt.type,
      trueText: vt.trueText || '',
      falseText: vt.falseText || '',
      trueValue: vt.trueValue || '',
      falseValue: vt.falseValue || '',
    };
  }
  defineExpose({ validate, normalize: () => {}, clean: () => {}, serialize });
</script>
