<template>
  <PropertyInputNumber
    v-if="isNumber"
    ref="inputRef"
    :value="value"
    :valueType="valueType"
    :disabled="disabled"
    @update:value="(v) => emit('update:value', v)"
  />
  <PropertyInputText
    v-else-if="valueType?.type === 'string'"
    ref="inputRef"
    :value="value"
    :valueType="valueType"
    :disabled="disabled"
    @update:value="(v) => emit('update:value', v)"
  />
  <PropertyInputBoolean
    v-else-if="valueType?.type === 'boolean'"
    ref="inputRef"
    :value="value"
    :valueType="valueType"
    :disabled="disabled"
    @update:value="(v) => emit('update:value', v)"
  />
  <PropertyInputDate
    v-else-if="valueType?.type === 'date'"
    ref="inputRef"
    :value="value"
    :valueType="valueType"
    :disabled="disabled"
    @update:value="(v) => emit('update:value', v)"
  />
  <PropertyInputEnum
    v-else-if="valueType?.type === 'enum'"
    ref="inputRef"
    :value="value"
    :valueType="valueType"
    :disabled="disabled"
    @update:value="(v) => emit('update:value', v)"
  />
  <PropertyInputArray
    v-else-if="valueType?.type === 'array'"
    ref="inputRef"
    :value="value"
    :valueType="valueType"
    :disabled="disabled"
    @update:value="(v) => emit('update:value', v)"
  />
  <PropertyInputObject
    v-else-if="valueType?.type === 'object'"
    ref="inputRef"
    :value="value"
    :valueType="valueType"
    :disabled="disabled"
    @update:value="(v) => emit('update:value', v)"
  />
  <a-input v-else :value="value" :disabled="disabled" @update:value="(v) => emit('update:value', v)" />
</template>

<script setup>
  import { computed, ref } from 'vue';
  import PropertyInputNumber from './property-input-number.vue';
  import PropertyInputText from './property-input-text.vue';
  import PropertyInputBoolean from './property-input-boolean.vue';
  import PropertyInputDate from './property-input-date.vue';
  import PropertyInputEnum from './property-input-enum.vue';
  import PropertyInputArray from './property-input-array.vue';
  import PropertyInputObject from './property-input-object.vue';

  const props = defineProps({ value: { default: null }, valueType: { type: Object, required: true }, disabled: { type: Boolean, default: false } });
  const emit = defineEmits(['update:value']);

  const inputRef = ref();
  const isNumber = computed(() => ['int', 'long', 'float', 'double'].includes(props.valueType?.type));

  function validate() {
    return inputRef.value?.validate?.() || [];
  }
  defineExpose({ validate });
</script>
