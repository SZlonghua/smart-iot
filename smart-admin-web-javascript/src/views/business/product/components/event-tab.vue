<template>
  <div>
    <div style="margin-bottom: 12px">
      <a-button type="primary" @click="showModal()"
        ><template #icon><PlusOutlined /></template>新增事件</a-button
      >
    </div>

    <a-table size="small" :dataSource="events" :columns="columns" rowKey="id" bordered :pagination="false">
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'type'">
          <a-tag :color="typeColor[record.type]">{{ record.type }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <a-button type="link" size="small" @click="viewRecord = record">查看</a-button>
          <a-button type="link" size="small" @click="showModal(record)">编辑</a-button>
          <a-button danger type="link" size="small" @click="onDelete(record)">删除</a-button>
        </template>
      </template>
    </a-table>

    <a-modal :title="editingId ? '编辑事件' : '新增事件'" :open="visible" @ok="onSubmit" @cancel="onClose" width="700px" :destroyOnClose="true">
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
        <a-form-item label="标识" name="id"><a-input v-model:value="form.id" placeholder="唯一标识" /></a-form-item>
        <a-form-item label="名称" name="name"><a-input v-model:value="form.name" placeholder="事件名称" /></a-form-item>
        <a-form-item label="事件类型" name="type">
          <a-radio-group v-model:value="form.type">
            <a-radio value="info">信息(info)</a-radio>
            <a-radio value="warning">告警(warning)</a-radio>
            <a-radio value="error">故障(error)</a-radio>
          </a-radio-group>
        </a-form-item>
        <ValueTypeEditor ref="vtRef" :value="form.valueType" @update:value="(v) => (form.valueType = v)" />
        <a-form-item label="描述"><a-textarea v-model:value="form.description" placeholder="描述" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <EventViewModal :record="viewRecord" :visible="!!viewRecord" @close="viewRecord = null" />
  </div>
</template>

<script setup>
  import { ref, reactive, watch, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import ValueTypeEditor from '/@/components/business/value-type-editor/index.vue';
  import EventViewModal from './event-view-modal.vue';
  import _ from 'lodash';

  const props = defineProps({ modelValue: { type: Array, default: () => [] } });
  const emit = defineEmits(['update:modelValue']);

  const columns = [
    { title: '标识', dataIndex: 'id', width: 150 },
    { title: '名称', dataIndex: 'name', width: 150 },
    { title: '类型', dataIndex: 'type', width: 80 },
    { title: '描述', dataIndex: 'description' },
    { title: '操作', dataIndex: 'action', width: 150 },
  ];

  const typeColor = { info: 'blue', warning: 'orange', error: 'red' };

  const events = ref([...props.modelValue]);
  watch(
    () => props.modelValue,
    (v) => {
      events.value = [...v];
    }
  );

  const visible = ref(false);
  const editingId = ref(null);
  const viewRecord = ref(null); // 查看弹窗数据

  const formRef = ref();
  const vtRef = ref();
  const formDefault = { id: '', name: '', type: 'info', description: '', valueType: null };
  const form = reactive(_.cloneDeep(formDefault));

  const rules = {
    id: [{ required: true, message: '标识不能为空' }],
    name: [{ required: true, message: '名称不能为空' }],
    type: [{ required: true, message: '事件类型不能为空' }],
  };

  function showModal(record) {
    Object.assign(form, _.cloneDeep(formDefault));
    if (record) {
      editingId.value = record.id;
      const copy = _.cloneDeep(record);
      Object.assign(form, copy);
    } else {
      editingId.value = null;
    }
    visible.value = true;
    nextTick(() => vtRef.value?.clean?.(form.valueType)); // 极值归空
  }

  function onClose() {
    visible.value = false;
  }

  function onSubmit() {
    formRef.value
      .validate()
      .then(() => {
        const vtErr = vtRef.value?.validate();
        if (vtErr) {
          vtErr.forEach((msg) => message.warning(msg));
          return;
        }

        const data = _.cloneDeep({ ...form });
        if (!data.description) delete data.description;
        data.valueType = vtRef.value?.normalize() || data.valueType; // 归一化 + 获取最终值 ,处理极值和空值

        const list = events.value;
        const idx = list.findIndex((e) => e.id === editingId.value);
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

  function onDelete(record) {
    const list = events.value.filter((e) => e.id !== record.id);
    emit('update:modelValue', list);
  }
</script>
