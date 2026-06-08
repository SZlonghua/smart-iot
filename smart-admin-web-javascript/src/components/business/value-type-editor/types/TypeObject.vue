<!-- 对象类型编辑器 -->
<template>
  <a-form-item label="子属性">
    <div v-for="(prop, idx) in value.properties" :key="idx" style="border: 1px solid #e8e8e8; padding: 12px; margin-bottom: 8px; border-radius: 4px">
      <div style="display: flex; gap: 8px; margin-bottom: 8px; align-items: flex-start">
        <a-input v-model:value="prop.id" placeholder="标识*" style="width: 120px" :disabled="disabled" />
        <a-input v-model:value="prop.name" placeholder="名称*" style="width: 120px" :disabled="disabled" />
        <a-button
          v-if="!disabled"
          danger
          size="small"
          style="flex-shrink: 0"
          @click="
            value.properties.splice(idx, 1);
            propRefs.splice(idx, 1);
          "
          >×</a-button
        >
      </div>
      <ValueTypeEditor
        :ref="
          (el) => {
            propRefs[idx] = el;
          }
        "
        :value="prop.valueType"
        :allowedTypes="basicTypes"
        :disabled="disabled"
        @update:value="(v) => (prop.valueType = v)"
      />
    </div>
    <a-button v-if="!disabled" type="dashed" size="small" block @click="addProperty">+ 添加子属性</a-button>
  </a-form-item>
</template>

<script setup>
  import { ref, watch, defineAsyncComponent } from 'vue';
  const ValueTypeEditor = defineAsyncComponent(() => import('../index.vue'));
  const props = defineProps({ value: { type: Object, required: true }, disabled: Boolean });
  const basicTypes = ['int', 'double', 'float', 'long', 'boolean', 'string', 'enum', 'date'];
  const propRefs = ref([]); // 子属性的 ValueTypeEditor 实例
  watch(
    () => props.value.type,
    (t) => {
      if (t && !props.value.properties) props.value.properties = [];
    },
    { immediate: true }
  );
  function addProperty() {
    props.value.properties.push({ id: '', name: '', valueType: { type: 'string' } });
  }
  function normalize() {
    propRefs.value.forEach((r) => r?.normalize?.());
  }
  function clean() {
    propRefs.value.forEach((r) => r?.clean?.());
  }
  function validate() {
    if (!props.value.properties?.length) return ['对象类型至少需要一条子属性'];
    const err = [];
    props.value.properties.forEach((p, i) => {
      if (!p.id) err.push(`子属性${i + 1}的标识不能为空`);
      if (!p.name) err.push(`子属性${i + 1}的名称不能为空`);
    });
    return err.length ? err : null;
  }
  function serialize(vt) {
    const r = { type: vt.type };
    const ps = propRefs.value.map((ref) => ref?.serialize?.()).filter(Boolean);
    if (ps.length) r.properties = ps;
    return r;
  }
  defineExpose({ validate, normalize, clean, serialize });
</script>
