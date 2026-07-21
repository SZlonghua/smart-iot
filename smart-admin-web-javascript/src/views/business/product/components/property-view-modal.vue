<!-- 属性查看弹窗：与编辑页相同排版，全部禁用 -->
<template>
  <a-modal title="查看属性" :open="visible" :footer="null" @cancel="$emit('close')" width="700px" :destroyOnClose="true">
    <a-form :label-col="{ span: 5 }">
      <a-form-item label="标识"><a-input :value="record.id" disabled /></a-form-item>
      <a-form-item label="名称"><a-input :value="record.name" disabled /></a-form-item>
      <a-form-item label="读写权限">
        <a-radio-group :value="record.accessMode" disabled>
          <a-radio value="r">只读(r)</a-radio>
          <a-radio value="rw">读写(rw)</a-radio>
          <a-radio value="w">只写(w)</a-radio>
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
