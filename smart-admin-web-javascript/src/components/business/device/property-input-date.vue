<template>
  <a-date-picker v-model:value="val" :format="pickerFmt" :show-time="hasTime" style="width: 100%" :disabled="disabled" @change="onChange" />
</template>

<script setup>
  import { ref, computed, onMounted } from 'vue';
  import dayjs from 'dayjs';

  const props = defineProps({ value: { default: null }, valueType: { type: Object, required: true }, disabled: { type: Boolean, default: false } });
  const emit = defineEmits(['update:value']);

  const modelFmt = computed(() => props.valueType?.format || 'yyyy-MM-dd HH:mm:ss');
  const pickerFmt = computed(() => modelFmt.value.replace(/yyyy/g, 'YYYY').replace(/dd/g, 'DD'));
  const hasTime = computed(() => pickerFmt.value.includes('HH'));

  const val = ref(null);
  onMounted(() => {
    val.value = props.value && typeof props.value === 'string' ? dayjs(props.value, pickerFmt.value) : null;
  });

  function onChange(d) {
    emit('update:value', d ? d.format(pickerFmt.value) : null);
  }

  function validate() {
    return [];
  }
  defineExpose({ validate });
</script>
