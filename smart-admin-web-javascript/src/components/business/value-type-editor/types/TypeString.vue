<!-- 字符串类型编辑器 -->
<template>
  <a-form-item label="最大长度"
    ><a-input-number v-model:value="value.maxLength" style="width: 100%" :min="0" placeholder="不限制" :disabled="disabled"
  /></a-form-item>
</template>
<script setup>
  import { watch } from 'vue';
  const props = defineProps({ value: { type: Object, required: true }, disabled: Boolean });
  watch(
    () => props.value.type,
    (t) => {
      if (t && props.value.maxLength === undefined) props.value.maxLength = null;
    },
    { immediate: true }
  );
  function serialize(vt) {
    const r = { type: vt.type };
    if (vt.maxLength != null) r.maxLength = vt.maxLength;
    return r;
  }
  defineExpose({ validate: () => null, normalize: () => {}, clean: () => {}, serialize });
</script>
