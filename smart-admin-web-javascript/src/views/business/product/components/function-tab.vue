<template>
  <div>
    <div style="margin-bottom: 12px">
      <a-button type="primary" @click="showModal()"
        ><template #icon><PlusOutlined /></template>新增功能</a-button
      >
    </div>

    <a-table size="small" :dataSource="functions" :columns="columns" rowKey="id" bordered :pagination="false">
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'async'">{{ record.async ? '异步' : '同步' }}</template>
        <template v-if="column.dataIndex === 'action'">
          <a-button type="link" size="small" @click="viewRecord = record">查看</a-button>
          <a-button type="link" size="small" @click="showModal(record)">编辑</a-button>
          <a-button danger type="link" size="small" @click="onDelete(record)">删除</a-button>
        </template>
      </template>
    </a-table>

    <a-modal :title="editingId ? '编辑功能' : '新增功能'" :open="visible" @ok="onSubmit" @cancel="onClose" width="700px" :destroyOnClose="true">
      <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
        <a-form-item label="标识" name="id"><a-input v-model:value="form.id" placeholder="唯一标识" /></a-form-item>
        <a-form-item label="名称" name="name"><a-input v-model:value="form.name" placeholder="功能名称" /></a-form-item>
        <a-form-item label="是否异步"><a-switch v-model:checked="form.async" /></a-form-item>
        <!-- 输入参数：动态列表，每项含 ValueTypeEditor -->
        <a-form-item label="输入参数">
          <div v-for="(input, idx) in form.inputs" :key="idx" style="border: 1px solid #e8e8e8; padding: 8px; margin-bottom: 8px; border-radius: 4px">
            <div style="display: flex; gap: 8px; margin-bottom: 4px; align-items: center">
              <a-input v-model:value="input.id" placeholder="标识*" style="width: 100px" />
              <a-input v-model:value="input.name" placeholder="名称*" style="width: 100px" />
              <a-switch v-model:checked="input.required" checked-children="必填" un-checked-children="可选" />
              <a-button danger size="small" @click="form.inputs.splice(idx, 1)">×</a-button>
            </div>
            <ValueTypeEditor
              :ref="
                (el) => {
                  vtRefs[idx] = el;
                }
              "
              :value="input.valueType"
              @update:value="(v) => (input.valueType = v)"
            />
          </div>
          <a-button type="dashed" size="small" block @click="addInput">+ 添加输入参数</a-button>
        </a-form-item>
        <!-- 输出类型：可选，按需添加 -->
        <a-form-item label="输出类型">
          <template v-if="form.output">
            <div style="border: 1px solid #e8e8e8; padding: 8px; margin-bottom: 8px; border-radius: 4px">
              <div style="text-align: right; margin-bottom: 4px">
                <a-button danger size="small" @click="removeOutput">× 移除</a-button>
              </div>
              <ValueTypeEditor ref="outputVtRef" :value="form.output" @update:value="(v) => (form.output = v)" />
            </div>
          </template>
          <a-button v-else type="dashed" size="small" block @click="addOutput">+ 添加输出类型</a-button>
        </a-form-item>
        <a-form-item label="描述"><a-textarea v-model:value="form.description" placeholder="描述" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <FunctionViewModal :record="viewRecord" :visible="!!viewRecord" @close="viewRecord = null" />
  </div>
</template>

<script setup>
  import { ref, reactive, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import ValueTypeEditor from '/@/components/business/value-type-editor/index.vue';
  import FunctionViewModal from './function-view-modal.vue';
  import _ from 'lodash';

  const props = defineProps({ modelValue: { type: Array, default: () => [] } });
  const emit = defineEmits(['update:modelValue']);

  const columns = [
    { title: '标识', dataIndex: 'id', width: 150 },
    { title: '名称', dataIndex: 'name', width: 150 },
    { title: '类型', dataIndex: 'async', width: 80 },
    { title: '描述', dataIndex: 'description' },
    { title: '操作', dataIndex: 'action', width: 150 },
  ];

  const functions = ref([...props.modelValue]);
  watch(
    () => props.modelValue,
    (v) => {
      functions.value = [...v];
    }
  );

  const visible = ref(false);
  const editingId = ref(null);
  const viewRecord = ref(null); // 查看弹窗数据

  const formRef = ref();
  const vtRefs = ref([]); // 动态输入列表的 ValueTypeEditor 实例
  const outputVtRef = ref(); // 输出类型的 ValueTypeEditor 实例
  const formDefault = { id: '', name: '', async: false, description: '', inputs: [], output: null };
  const form = reactive(_.cloneDeep(formDefault));

  const rules = {
    id: [{ required: true, message: '标识不能为空' }],
    name: [{ required: true, message: '名称不能为空' }],
  };

  function showModal(record) {
    Object.assign(form, _.cloneDeep(formDefault));
    if (record) {
      editingId.value = record.id;
      Object.assign(form, _.cloneDeep(record));
    } else {
      editingId.value = null;
    }
    visible.value = true;
  }

  function onClose() {
    visible.value = false;
  }

  function onSubmit() {
    // 1. 基础字段校验
    formRef.value
      .validate()
      .then(() => {
        // 2. 输入参数校验（通过 ref 调用 ValueTypeEditor.validate()）
        if (form.inputs) {
          for (let i = 0; i < form.inputs.length; i++) {
            if (!form.inputs[i].id || !form.inputs[i].name) {
              message.warning('输入参数的标识和名称不能为空');
              return;
            }
            const err = vtRefs.value[i]?.validate();
            if (err) {
              err.forEach((msg) => message.warning('输入参数：' + msg));
              return;
            }
          }
        }
        // 3. 输出类型校验
        if (form.output?.type) {
          const outputErr = outputVtRef.value?.validate();
          if (outputErr) {
            outputErr.forEach((msg) => message.warning('输出类型：' + msg));
            return;
          }
        }

        const data = _.cloneDeep({ ...form });
        if (!data.description) delete data.description;
        if (!data.inputs || data.inputs.length === 0) delete data.inputs;
        // 4. 输入参数归一化,处理极值和空值
        if (data.inputs) {
          data.inputs.forEach((input, i) => {
            input.valueType = vtRefs.value[i]?.normalize() || input.valueType;
          });
        }
        // 5. 输出类型归一化,处理极值和空值
        if (data.output?.type) {
          data.output = outputVtRef.value?.normalize() || data.output;
        } else {
          delete data.output;
        }

        const list = functions.value;
        const idx = list.findIndex((f) => f.id === editingId.value);
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
    const list = functions.value.filter((f) => f.id !== record.id);
    emit('update:modelValue', list);
  }

  function addInput() {
    form.inputs.push({ id: '', name: '', required: false, valueType: null });
  }
  function addOutput() {
    form.output = { type: undefined };
  }
  function removeOutput() {
    form.output = null;
  }
</script>
