<template>
  <a-input
    :value="selectedName"
    placeholder="请选择产品"
    :readOnly="true"
    :disabled="disabled"
    @click="!disabled && openModal()"
    :style="{ cursor: disabled ? 'not-allowed' : 'pointer' }"
  />

  <a-modal v-model:open="visible" :width="900" title="选择产品" @cancel="closeModal" @ok="onConfirm">
    <a-form class="smart-query-form">
      <a-row class="smart-query-form-row">
        <a-form-item label="产品名称" class="smart-query-form-item">
          <a-input style="width: 200px" v-model:value="params.name" placeholder="产品名称" @pressEnter="onSearch" />
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
        <template v-if="column.dataIndex === 'deviceType'">
          <span>{{ $smartEnumPlugin.getDescByValue('DEVICE_TYPE_ENUM', text) }}</span>
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
  import { productApi } from '/@/api/business/product/product-api';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';

  const props = defineProps({
    value: [Number, String],
    disabled: { type: Boolean, default: false },
  });

  const emit = defineEmits(['update:value', 'select']);

  const selectedName = ref('');
  const selectedRow = ref(null);
  const visible = ref(false);

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
      message.warning('请选择产品');
      return;
    }
    selectedName.value = selectedRow.value.name;
    emit('update:value', selectedRow.value.id);
    emit('select', selectedRow.value.id, selectedRow.value);
    closeModal();
  }

  const tableLoading = ref(false);
  const total = ref(0);

  const defaultParams = {
    name: '',
    status: 1, // 只查启用的产品
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
      let res = await productApi.queryPage({ ...params });
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
      if (val) {
        loadSelected();
      } else {
        selectedName.value = '';
      }
    },
    { immediate: true }
  );

  async function loadSelected() {
    try {
      const res = await productApi.getById(props.value);
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
    { title: '产品名称', dataIndex: 'name', resizable: true, width: 180 },
    { title: '产品Key', dataIndex: 'productKey', resizable: true, width: 180 },
    { title: '产品分类', dataIndex: 'categoryName', resizable: true, width: 120 },
    { title: '设备类型', dataIndex: 'deviceType', resizable: true, width: 100 },
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
