<template>
  <div>
    <a-textarea v-model:value="val" :rows="4" placeholder="请输入数组，格式: [1,2,3] 或 [a,b]" :disabled="disabled" />
    <div v-if="err" style="color: #ff4d4f; font-size: 12px; margin-top: 4px">{{ err }}</div>
  </div>
</template>

<script setup>
  import { ref, watch, onMounted } from 'vue';
  import { validateValue } from './validate-value.js';

  const props = defineProps({ value: { default: null }, valueType: { type: Object, required: true }, disabled: { type: Boolean, default: false } });
  const emit = defineEmits(['update:value']);
  const val = ref('');
  const err = ref('');

  function toDisplayStr(v) {
    if (v === null || v === undefined) return '';
    if (typeof v === 'string') return v;
    if (Array.isArray(v)) return JSON.stringify(v);
    return String(v);
  }
  onMounted(() => {
    val.value = toDisplayStr(props.value);
  });

  // 输入变化 → 解析为数组 emit，string/enum/boolean 元素还原为字符串防丢类型
  watch(val, (v) => {
    if (!v) {
      emit('update:value', null);
      return;
    }

    let arr;
    try {
      arr = JSON.parse(v);
    } catch {
      emit('update:value', v);
      return;
    }

    if (!Array.isArray(arr)) {
      emit('update:value', v);
      return;
    }

    const needStr = ['string', 'enum', 'boolean'].includes(props.valueType?.elementType?.type);
    emit('update:value', needStr ? arr.map(String) : arr);
    err.value = '';
  });

  function validate() {
    const e = validateValue(val.value, props.valueType);
    return e ? [e] : [];
  }

  defineExpose({ validate });
</script>
