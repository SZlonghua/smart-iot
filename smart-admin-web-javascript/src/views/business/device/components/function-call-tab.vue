<template>
  <a-tabs v-model:activeKey="mode" size="small">
    <a-tab-pane key="simple" tab="精简模式">
      <FunctionCallSimple :deviceId="deviceId" :functions="functions" />
    </a-tab-pane>
    <a-tab-pane key="advanced" tab="高级模式">
      <FunctionCallAdvanced :deviceId="deviceId" :functions="functions" />
    </a-tab-pane>
  </a-tabs>
</template>

<script setup>
  import { ref } from 'vue';
  import FunctionCallSimple from './function-call-simple.vue';
  import FunctionCallAdvanced from './function-call-advanced.vue';

  defineProps({ deviceId: { type: Number, required: true }, functions: { type: Array, default: () => [] } });

  const mode = ref('simple');
</script>
