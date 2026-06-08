<template>
  <div>
    <div style="margin-bottom: 12px">
      <a-button type="primary" @click="showModal()"
        ><template #icon><PlusOutlined /></template>新增属性</a-button
      >
    </div>

    <!-- 属性列表 -->
    <a-table size="small" :dataSource="properties" :columns="columns" rowKey="id" bordered :pagination="false">
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'valueType'">{{ record.valueType?.type }}</template>
        <template v-if="column.dataIndex === 'accessMode'">
          <a-tag :color="accessModeColor[record.accessMode]">{{ accessModeMap[record.accessMode] }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <a-button type="link" size="small" @click="viewRecord = record">查看</a-button>
          <a-button type="link" size="small" @click="showModal(record)">编辑</a-button>
          <a-button danger type="link" size="small" @click="onDelete(record)">删除</a-button>
        </template>
      </template>
    </a-table>

    <!-- 新增/编辑弹窗 -->
    <a-modal :title="editingId ? '编辑属性' : '新增属性'" :open="visible" @ok="onSubmit" @cancel="onClose" width="700px" :destroyOnClose="true">
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
        <a-form-item label="标识" name="id"><a-input v-model:value="form.id" placeholder="唯一标识" /></a-form-item>
        <a-form-item label="名称" name="name"><a-input v-model:value="form.name" placeholder="属性名称" /></a-form-item>
        <a-form-item label="读写权限" name="accessMode">
          <a-radio-group v-model:value="form.accessMode">
            <a-radio value="r">只读(r)</a-radio>
            <a-radio value="rw">读写(rw)</a-radio>
            <a-radio value="w">只写(w)</a-radio>
          </a-radio-group>
        </a-form-item>
        <!-- 值类型编辑器：动态分发到 TypeXxx 组件 -->
        <ValueTypeEditor ref="vtRef" :value="form.valueType" @update:value="(v) => (form.valueType = v)" />
        <a-form-item label="描述"><a-textarea v-model:value="form.description" placeholder="描述" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看弹窗（独立组件） -->
    <PropertyViewModal :record="viewRecord" :visible="!!viewRecord" @close="viewRecord = null" />
  </div>
</template>

<script setup>
  import { ref, reactive, watch, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import ValueTypeEditor from '/@/components/business/value-type-editor/index.vue';
  import PropertyViewModal from './property-view-modal.vue';
  import _ from 'lodash';

  const props = defineProps({ modelValue: { type: Array, default: () => [] } });
  const emit = defineEmits(['update:modelValue']);

  // ---- 表格列定义 ----
  const columns = [
    { title: '标识', dataIndex: 'id', width: 150 },
    { title: '名称', dataIndex: 'name', width: 150 },
    { title: '类型', dataIndex: 'valueType', width: 100 },
    { title: '权限', dataIndex: 'accessMode', width: 80 },
    { title: '描述', dataIndex: 'description' },
    { title: '操作', dataIndex: 'action', width: 150 },
  ];

  const accessModeMap = { r: '只读', rw: '读写', w: '只写' };
  const accessModeColor = { r: 'blue', rw: 'green', w: 'orange' };

  // ---- 响应式数据 ----
  const properties = ref([...props.modelValue]);
  watch(
    () => props.modelValue,
    (v) => {
      properties.value = [...v];
    }
  );

  const visible = ref(false);
  const editingId = ref(null);
  const viewRecord = ref(null); // 查看弹窗数据

  // ---- 表单 ----
  const formRef = ref();
  const vtRef = ref(); // ValueTypeEditor 实例
  const formDefault = { id: '', name: '', accessMode: 'rw', description: '', valueType: null };
  const form = reactive({ ...formDefault });

  // 基础字段校验（Ant Design 表单 rules）
  const rules = {
    id: [{ required: true, message: '标识不能为空' }],
    name: [{ required: true, message: '名称不能为空' }],
    accessMode: [{ required: true, message: '读写权限不能为空' }],
  };

  // ---- 弹窗 ----
  function showModal(record) {
    Object.assign(form, formDefault);
    if (record) {
      editingId.value = record.id;
      Object.assign(form, _.cloneDeep(record));
    } else {
      editingId.value = null;
    }
    visible.value = true;
    nextTick(() => vtRef.value?.clean?.(form.valueType)); // 极值归空
  }

  function onClose() {
    visible.value = false;
  }

  // ---- 提交 ----
  function onSubmit() {
    // 1. Ant Design 表单校验基础字段
    formRef.value
      .validate()
      .then(async () => {
        // 2. 值类型校验（通过 ValueTypeEditor 实例）
        const vtErr = vtRef.value?.validate();
        if (vtErr) {
          vtErr.forEach((msg) => message.warning(msg));
          return;
        }

        const data = _.cloneDeep({ ...form });
        if (!data.description) delete data.description; // 空则不留
        data.valueType = vtRef.value?.normalize() || data.valueType; // 归一化 + 获取最终值 ,处理极值和空值

        const list = properties.value;
        const idx = list.findIndex((p) => p.id === editingId.value);

        if (idx >= 0) {
          list.splice(idx, 1, data);
        } else {
          list.push(data);
        }
        emit('update:modelValue', [...list]);
        visible.value = false;
      })
      .catch(() => {});
  }

  // ---- 删除 ----
  function onDelete(record) {
    const list = properties.value.filter((p) => p.id !== record.id);
    emit('update:modelValue', list);
  }
</script>
