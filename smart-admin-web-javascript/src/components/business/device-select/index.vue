<template>
  <a-input
    :value="selectedName"
    :placeholder="placeholder"
    :readOnly="true"
    :disabled="disabled"
    @click="!disabled && openModal()"
    :style="{ cursor: disabled ? 'not-allowed' : 'pointer', width: width }"
  />

  <a-modal v-model:open="visible" :width="900" title="选择设备" @cancel="closeModal" @ok="onConfirm">
    <a-form class="smart-query-form">
      <a-row class="smart-query-form-row">
        <a-form-item label="设备名称" class="smart-query-form-item">
          <a-input style="width: 200px" v-model:value="params.name" placeholder="设备名称" @pressEnter="onSearch" />
        </a-form-item>
        <a-form-item class="smart-query-form-item smart-margin-left10">
          <a-button type="primary" @click="onSearch">
            <template #icon><SearchOutlined /></template>查询
          </a-button>
          <a-button @click="reset" class="smart-margin-left10">
            <template #icon><ReloadOutlined /></template>重置
          </a-button>
        </a-form-item>
      </a-row>
    </a-form>
    <a-table
      :loading="tableLoading"
      size="small"
      :columns="columns"
      :data-source="tableData"
      :pagination="false"
      bordered
      rowKey="id"
      :scroll="{ y: 300 }"
      :customRow="customRow"
    >
      <template #bodyCell="{ text, column, record }">
        <template v-if="column.dataIndex === 'selectedRow'">
          <a-radio :checked="selectedRow && selectedRow.id === record.id" @click="selectedRow = record" />
        </template>
        <template v-if="column.dataIndex === 'status'">
          <span>{{ $smartEnumPlugin.getDescByValue('DEVICE_STATUS_ENUM', text) }}</span>
        </template>
      </template>
    </a-table>
    <div class="smart-query-table-page">
      <a-pagination
        showSizeChanger
        showQuickJumper
        show-less-items
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        :defaultPageSize="params.pageSize"
        v-model:current="params.pageNum"
        v-model:pageSize="params.pageSize"
        :total="total"
        @change="queryData"
        :show-total="(total) => `共${total}条`"
      />
    </div>
  </a-modal>
</template>

<script setup>
  import { ref, reactive, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { deviceApi } from '/@/api/business/device/device-api';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';

  // value = 选中设备 ID（device 表主键，与消息日志时序库 device_id 维度同源）
  const props = defineProps({
    value: [Number, String],
    placeholder: { type: String, default: '请选择设备' },
    width: { type: String, default: '100%' },
    disabled: { type: Boolean, default: false },
  });

  const emit = defineEmits(['update:value', 'select']);

  // 选中设备名称（输入框展示；选择产生，重置清空）
  const selectedName = ref('');
  const selectedRow = ref(null);
  const visible = ref(false);
  // 本组件选择产生的值（已携带名称）— 仅外部回设值（编辑回显）时才需按 ID 拉取名称
  const pickedId = ref(null);

  function openModal() {
    visible.value = true;
    onSearch();
  }

  function closeModal() {
    visible.value = false;
    selectedRow.value = null;
  }

  function onConfirm() {
    if (!selectedRow.value) {
      message.warning('请选择设备');
      return;
    }
    selectedName.value = selectedRow.value.name;
    pickedId.value = selectedRow.value.id;
    emit('update:value', selectedRow.value.id);
    emit('select', selectedRow.value.id, selectedRow.value);
    closeModal();
  }

  const tableLoading = ref(false);
  const total = ref(0);

  // 设备列表不过滤状态（离线设备同样可选 — 日志查询常态就是查离线设备的历史记录）
  const defaultParams = {
    name: '',
    pageNum: 1,
    pageSize: PAGE_SIZE,
  };
  const params = reactive({ ...defaultParams });

  function reset() {
    Object.assign(params, defaultParams);
    queryData();
  }

  function onSearch() {
    params.pageNum = 1;
    queryData();
  }

  async function queryData() {
    tableLoading.value = true;
    try {
      let res = await deviceApi.queryPage({ ...params });
      tableData.value = res.data.list || [];
      total.value = res.data.total;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  watch(
    () => props.value,
    (val) => {
      if (!val) {
        selectedName.value = '';
      } else if (String(val) !== String(pickedId.value)) {
        loadSelected();
      }
    },
    { immediate: true }
  );

  // 外部回设值 → 按 ID 拉取设备名称回显
  async function loadSelected() {
    try {
      const res = await deviceApi.getDetail(props.value);
      if (res.data) {
        selectedName.value = res.data.name;
      }
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  const tableData = ref([]);
  const columns = [
    { title: '', dataIndex: 'selectedRow', width: 40 },
    { title: '设备名称', dataIndex: 'name', resizable: true, width: 150 },
    { title: 'Device Key', dataIndex: 'deviceKey', resizable: true, width: 180 },
    { title: '产品名称', dataIndex: 'productName', resizable: true, width: 120 },
    { title: '状态', dataIndex: 'status', resizable: true, width: 80 },
  ];

  function customRow(record) {
    return {
      onClick: () => {
        selectedRow.value = record;
      },
      style: { cursor: 'pointer' },
    };
  }
</script>
