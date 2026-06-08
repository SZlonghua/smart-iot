<!-- 枚举类型编辑器 -->
<template>
  <a-form-item label="枚举项" required>
    <div v-for="(el, idx) in value.elements" :key="idx" style="display: flex; gap: 8px; margin-bottom: 8px">
      <a-input v-model:value="el.value" placeholder="值*" style="width: 100px" :disabled="disabled" />
      <a-input v-model:value="el.text" placeholder="文本*" style="width: 120px" :disabled="disabled" />
      <a-input v-model:value="el.description" placeholder="说明" style="flex: 1" :disabled="disabled" />
      <a-button v-if="!disabled" danger size="small" @click="value.elements.splice(idx, 1)">×</a-button>
    </div>
    <a-button v-if="!disabled" type="dashed" size="small" block @click="addElement">+ 添加枚举项</a-button>
  </a-form-item>
</template>

<script setup>
  import { watch, onMounted } from 'vue';
  const props = defineProps({ value: { type: Object, required: true }, disabled: Boolean });
  onMounted(() => {
    if (!props.value.elements) props.value.elements = [];
  });
  function addElement() {
    props.value.elements.push({ value: '', text: '', description: '' });
  }
  function validate() {
    if (!props.value.elements?.length) return ['枚举类型至少需要一条枚举值'];
    const err = [];
    props.value.elements.forEach((e, i) => {
      if (!e.value) err.push(`枚举项${i + 1}的值不能为空`);
      if (!e.text) err.push(`枚举项${i + 1}的文本不能为空`);
    });
    return err.length ? err : null;
  }
  function serialize(vt) {
    const r = { type: vt.type };
    if (vt.elements?.length) r.elements = vt.elements;
    return r;
  }

  const emit = defineEmits(['valueChange']);
  watch(
    () => props.value,
    () => emit('valueChange'),
    { deep: true }
  );

  defineExpose({ validate, normalize: () => {}, clean: () => {}, serialize });
</script>
