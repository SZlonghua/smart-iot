<template>
  <div class="json-editor">
    <div class="json-editor-lines" ref="linesRef">
      <div v-for="n in lineCount" :key="n" class="json-editor-line-num">{{ n }}</div>
    </div>
    <textarea
      ref="textareaRef"
      :value="value"
      @input="onInput"
      @scroll="syncScroll"
      :disabled="disabled"
      spellcheck="false"
      class="json-editor-text"
    ></textarea>
  </div>
</template>

<script setup>
  import { ref, computed } from 'vue';

  const props = defineProps({ value: { default: '' }, disabled: { type: Boolean, default: false } });
  const emit = defineEmits(['update:value']);

  const textareaRef = ref();
  const linesRef = ref();

  const lineCount = computed(() => (props.value || '').split('\n').length || 1);

  function onInput(e) {
    emit('update:value', e.target.value);
  }
  function syncScroll() {
    if (linesRef.value) linesRef.value.scrollTop = textareaRef.value.scrollTop;
  }
</script>

<style scoped>
  .json-editor {
    display: flex;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    overflow: hidden;
    height: 100%;
    background: #fff;
  }
  .json-editor:focus-within {
    border-color: #1677ff;
    box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.2);
  }
  .json-editor-lines {
    overflow: hidden;
    background: #fafafa;
    text-align: right;
    padding: 8px 0;
    user-select: none;
  }
  .json-editor-line-num {
    font-size: 13px;
    line-height: 20px;
    font-family: 'SFMono-Regular', Consolas, Menlo, monospace;
    color: #bfbfbf;
    padding: 0 12px;
  }
  .json-editor-text {
    flex: 1;
    border: none;
    outline: none;
    resize: none;
    padding: 8px 12px;
    font-size: 13px;
    line-height: 20px;
    font-family: 'SFMono-Regular', Consolas, Menlo, monospace;
    color: #262626;
    background: #fff;
  }
  .json-editor-text:disabled {
    background: #fafafa;
    color: #8c8c8c;
  }
</style>
