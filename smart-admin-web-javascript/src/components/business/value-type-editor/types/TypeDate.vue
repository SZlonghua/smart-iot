<!-- 日期类型编辑器 -->
<template>
  <a-form-item label="日期格式" required
    ><a-input v-model:value="value.format" placeholder="如：yyyy-MM-dd HH:mm:ss" :disabled="disabled"
  /></a-form-item>
  <a-form-item label="时区" required><a-input v-model:value="value.tz" placeholder="如：Asia/Shanghai" :disabled="disabled" /></a-form-item>
</template>

<script setup>
  import { watch, onMounted } from 'vue';
  const props = defineProps({ value: { type: Object, required: true }, disabled: Boolean });
  onMounted(() => {
    if (props.value.format === undefined) props.value.format = 'yyyy-MM-dd HH:mm:ss';
    if (props.value.tz === undefined) props.value.tz = 'Asia/Shanghai';
  });
  function validate() {
    const err = [];
    if (!props.value.format) err.push('日期格式不能为空');
    if (!props.value.tz) err.push('时区不能为空');
    return err.length ? err : null;
  }
  function serialize(vt) {
    return { type: vt.type, format: vt.format || '', tz: vt.tz || '' };
  }

  const emit = defineEmits(['valueChange']);
  watch(
    () => props.value,
    () => emit('valueChange'),
    { deep: true }
  );

  defineExpose({ validate, normalize: () => {}, clean: () => {}, serialize });
</script>
