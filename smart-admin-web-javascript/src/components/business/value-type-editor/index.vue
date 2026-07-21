<template>
  <div class="value-type-editor">
    <a-form-item label="数据类型" required>
      <a-select v-model:value="localValue.type" :options="availableTypes" placeholder="请选择数据类型" :disabled="disabled" @change="onTypeChange" />
    </a-form-item>
    <!-- 子组件变更 → 通知 index → 序列化 → 通知父 -->
    <component :is="currentEditor" v-if="currentEditor" ref="editorRef" :value="localValue" :disabled="disabled" @valueChange="onChildChange" />
  </div>
</template>

<script setup>
  import { reactive, watch, computed, ref, nextTick } from 'vue';
  import { typeEditorMap, TYPE_OPTIONS } from './typeMap';
  import _ from 'lodash';

  const props = defineProps({
    value: { type: Object, default: null },
    allowedTypes: { type: Array, default: null },
    disabled: { type: Boolean, default: false },
  });

  const emit = defineEmits(['update:value']);

  const availableTypes = computed(() => (props.allowedTypes ? TYPE_OPTIONS.filter((t) => props.allowedTypes.includes(t.value)) : TYPE_OPTIONS));

  const currentEditor = computed(() => typeEditorMap[localValue.type]);
  const editorRef = ref(null);

  // 内部编辑副本：子组件直接修改此对象
  let localValue = reactive({ ...(props.value || {}) });

  // ---- 外部数据流入：props → localValue ----
  watch(
    () => props.value,
    (v) => {
      if (!v?.type) {
        Object.keys(localValue).forEach((k) => delete localValue[k]);
        return;
      }
      Object.assign(localValue, _.cloneDeep(v));
      nextTick(() => editorRef.value?.clean?.(localValue));
    },
    { deep: true, immediate: true }
  );

  // ---- 子组件通知 → 序列化 → 通知父组件 ----
  let lastEmit = '';
  function onChildChange() {
    const val = JSON.stringify(editorRef.value?.serialize?.(localValue) || localValue);
    // 没有变更 避免重复触发事件
    if (val === lastEmit) return;
    lastEmit = val;
    emit('update:value', editorRef.value?.serialize?.(localValue) || _.cloneDeep(localValue));
  }

  // ---- 用户切换类型：只设 type，子组件补完默认值后自动通知 ----
  function onTypeChange(newType) {
    Object.keys(localValue).forEach((k) => delete localValue[k]);
    localValue.type = newType;
  }

  // ---- 对外暴露方法 ----
  function validate() {
    if (!localValue.type) return ['数据类型不能为空'];
    return editorRef.value?.validate?.();
  }
  function normalize() {
    editorRef.value?.normalize?.(localValue);
    return editorRef.value?.serialize?.(localValue) || _.cloneDeep(localValue);
  }
  function clean() {
    editorRef.value?.clean?.(localValue);
  }
  function serialize() {
    return editorRef.value?.serialize?.(localValue) || _.cloneDeep(localValue);
  }
  defineExpose({ validate, normalize, clean, serialize });
</script>
