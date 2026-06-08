<!-- 功能查看弹窗：与编辑页相同排版，全部禁用 -->
<template>
  <a-modal title="查看功能" :open="visible" :footer="null" @cancel="$emit('close')" width="700px" :destroyOnClose="true">
    <a-form :label-col="{ span: 5 }">
      <a-form-item label="标识"><a-input :value="record.id" disabled /></a-form-item>
      <a-form-item label="名称"><a-input :value="record.name" disabled /></a-form-item>
      <a-form-item label="是否异步"><a-switch :checked="record.async" disabled /></a-form-item>
      <!-- 输入参数 -->
      <a-form-item v-if="record.inputs?.length" label="输入参数">
        <div v-for="(input, idx) in record.inputs" :key="idx" style="border: 1px solid #e8e8e8; padding: 8px; margin-bottom: 8px; border-radius: 4px">
          <div style="display: flex; gap: 8px; margin-bottom: 4px; align-items: center">
            <a-input :value="input.id" placeholder="标识" style="width: 100px" disabled />
            <a-input :value="input.name" placeholder="名称" style="width: 100px" disabled />
            <a-switch :checked="input.required" disabled />
          </div>
          <ValueTypeEditor :value="input.valueType" disabled />
        </div>
      </a-form-item>
      <!-- 输出类型 -->
      <a-form-item v-if="record.output?.type" label="输出类型">
        <div style="border: 1px solid #e8e8e8; padding: 8px; margin-bottom: 8px; border-radius: 4px">
          <ValueTypeEditor :value="record.output" disabled />
        </div>
      </a-form-item>
      <a-form-item label="描述"><a-textarea :value="record.description" disabled :rows="2" /></a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
  import ValueTypeEditor from '/@/components/business/value-type-editor/index.vue';
  defineProps({ record: Object, visible: Boolean });
  defineEmits(['close']);
</script>
