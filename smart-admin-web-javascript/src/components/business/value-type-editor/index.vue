<template>
  <div class="value-type-editor">
    <a-form-item label="数据类型" required>
      <a-select v-model:value="localValue.type" :options="availableTypes" placeholder="请选择数据类型" :disabled="disabled" @change="onTypeChange" />
    </a-form-item>
    <!-- 动态分发 + ref 用于父组件调用 validate -->
    <component :is="currentEditor" v-if="currentEditor" ref="editorRef" :value="localValue" :disabled="disabled" />
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

  let localValue = reactive({ ...(props.value || {}) });

  let internalUpdate = false;

  watch(
    () => props.value,
    (v) => {
      console.log('props.value', v);
      if (internalUpdate) {
        console.log('internalUpdate', internalUpdate);
        internalUpdate = false;
        return;
      }
      if (!v?.type) {
        console.log('no type, clear localValue');
        Object.keys(localValue).forEach((k) => delete localValue[k]);
        return;
      }
      const copy = _.cloneDeep(v);
      Object.assign(localValue, copy);
      internalUpdate = true;
      nextTick(() => editorRef.value?.clean?.(localValue)); // 极值归空（查看页直接挂载）
    },
    { deep: true, immediate: true }
  );

  // 用户切换类型：只设 type，字段默认值由子组件内部填充
  function onTypeChange(newType) {
    internalUpdate = true;
    Object.keys(localValue).forEach((k) => delete localValue[k]);
    localValue.type = newType;
  }

  let lastEmit = '';
  watch(
    localValue,
    () => {
      console.log('localValue', localValue);
      const val = JSON.stringify(localValue);
      if (val === lastEmit) return;
      lastEmit = val;
      internalUpdate = true;
      emit('update:value', editorRef.value?.serialize?.(localValue) || _.cloneDeep(localValue));
    },
    { deep: true }
  );

  // 对外暴露：validate / normalize / clean 委托给子组件，递归由子组件内部处理
  function validate() {
    if (!localValue.type) return ['数据类型不能为空'];
    return editorRef.value?.validate?.();
  }
  // 归一化：处理极值(min max不能超过取值范围)和空值--面向后台数据的
  function normalize() {
    editorRef.value?.normalize?.(localValue);
    // 序列化：将值转换为对象,将空值字段删除 只留有值的字段
    return editorRef.value?.serialize?.(localValue) || _.cloneDeep(localValue);
  }
  // 清空：将值(min max为极值时设为空值展示)设为空对象--面向界面的
  function clean() {
    editorRef.value?.clean?.(localValue);
  }
  defineExpose({ validate, normalize, clean });
</script>
