<!-- 事件查看弹窗：与编辑页相同排版，全部禁用 -->
<template>
  <a-modal title="查看事件" :open="visible" :footer="null" @cancel="$emit('close')" width="700px" :destroyOnClose="true">
    <a-form :label-col="{ span: 5 }">
      <a-form-item label="标识"><a-input :value="record.id" disabled /></a-form-item>
      <a-form-item label="名称"><a-input :value="record.name" disabled /></a-form-item>
      <a-form-item label="事件类型">
        <a-radio-group :value="record.type" disabled>
          <a-radio value="info">信息(info)</a-radio>
          <a-radio value="warning">告警(warning)</a-radio>
          <a-radio value="error">故障(error)</a-radio>
        </a-radio-group>
      </a-form-item>
      <ValueTypeEditor :value="record.valueType" disabled />
      <a-form-item label="描述"><a-textarea :value="record.description" disabled :rows="2" /></a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import ValueTypeEditor from '/@/components/business/value-type-editor/index.vue';
  defineProps({ record: Object, visible: Boolean });
  defineEmits(['close']);
</script>
