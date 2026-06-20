<template>
  <!---------- 查询表单 begin ---------->
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row">
      <a-form-item label="Device Key" class="smart-query-form-item">
        <a-input style="width: 200px" @pressEnter="onSearch" v-model:value="queryForm.deviceKey" placeholder="Device Key" />
      </a-form-item>
      <a-form-item label="设备名称" class="smart-query-form-item">
        <a-input style="width: 200px" @pressEnter="onSearch" v-model:value="queryForm.name" placeholder="设备名称" />
      </a-form-item>
      <a-form-item label="产品名称" class="smart-query-form-item">
        <a-input style="width: 200px" @pressEnter="onSearch" v-model:value="queryForm.productName" placeholder="产品名称" />
      </a-form-item>
      <a-form-item label="状态" class="smart-query-form-item">
        <SmartEnumSelect @pressEnter="onSearch" v-model:value="queryForm.status" enum-name="DEVICE_STATUS_ENUM" width="160px" placeholder="状态" />
      </a-form-item>
      <a-form-item class="smart-query-form-item">
        <a-button type="primary" @click="onSearch">
          <template #icon><SearchOutlined /></template>查询
        </a-button>
        <a-button @click="resetQuery" class="smart-margin-left10">
          <template #icon><ReloadOutlined /></template>重置
        </a-button>
      </a-form-item>
    </a-row>
  </a-form>
  <!---------- 查询表单 end ---------->

  <a-card size="small" :bordered="false" :hoverable="true">
    <a-row class="smart-table-btn-block">
      <div class="smart-table-operate-block">
        <a-button @click="showForm" type="primary">
          <template #icon><PlusOutlined /></template>新建设备
        </a-button>
        <a-button @click="confirmBatchDelete" type="primary" danger :disabled="selectedRowKeyList.length == 0">
          <template #icon><DeleteOutlined /></template>批量删除
        </a-button>
      </div>
      <div class="smart-table-setting-block">
        <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.BUSINESS.IOT.DEVICE" :refresh="queryData" />
      </div>
    </a-row>

    <!-- 表格 begin -->
    <a-table
      size="small"
      :scroll="{ y: 800 }"
      :dataSource="tableData"
      :columns="columns"
      rowKey="id"
      bordered
      :loading="tableLoading"
      :pagination="false"
      :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }"
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="statusColor(text)">{{ $smartEnumPlugin.getDescByValue('DEVICE_STATUS_ENUM', text) }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button @click="showForm(record)" type="link">编辑</a-button>
            <a-button @click="showView(record)" type="link">查看</a-button>
            <a-button @click="onDelete(record)" danger type="link">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>
    <!-- 表格 end -->

    <div class="smart-query-table-page">
      <a-pagination
        showSizeChanger
        showQuickJumper
        show-less-items
        :pageSizeOptions="PAGE_SIZE_OPTIONS"
        :defaultPageSize="queryForm.pageSize"
        v-model:current="queryForm.pageNum"
        v-model:pageSize="queryForm.pageSize"
        :total="total"
        @change="queryData"
        @showSizeChange="queryData"
        :show-total="(total) => `共${total}条`"
      />
    </div>

    <DeviceForm ref="formRef" @reloadList="queryData" />
  </a-card>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { message, Modal } from 'ant-design-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { deviceApi } from '/@/api/business/device/device-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import DeviceForm from './device-form-modal.vue';
  import _ from 'lodash';

  const router = useRouter();

  function statusColor(status) {
    const map = { 0: 'blue', 1: 'green', 2: 'red' };
    return map[status] || 'default';
  }

  // 表格列
  const columns = ref([
    { title: 'Device Key', dataIndex: 'deviceKey', resizable: true, width: 180 },
    { title: '设备名称', dataIndex: 'name', resizable: true, width: 150 },
    { title: '产品名称', dataIndex: 'productName', resizable: true, width: 120 },
    { title: '状态', dataIndex: 'status', resizable: true, width: 80 },
    { title: '描述', dataIndex: 'description', resizable: true, width: 200, ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', resizable: true, width: 170 },
    { title: '操作', dataIndex: 'action', fixed: 'right', width: 150 },
  ]);

  // 查询数据表单和方法
  const queryFormState = {
    name: '',
    deviceKey: '',
    productName: '',
    status: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  function resetQuery() {
    let pageSize = queryForm.pageSize;
    Object.assign(queryForm, queryFormState);
    queryForm.pageSize = pageSize;
    queryData();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    queryData();
  }

  async function queryData() {
    tableLoading.value = true;
    try {
      let queryResult = await deviceApi.queryPage({ ...queryForm });
      tableData.value = queryResult.data.list;
      total.value = queryResult.data.total;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  onMounted(queryData);

  const formRef = ref();
  function showForm(rowData) {
    formRef.value.showDrawer(rowData);
  }

  function showView(rowData) {
    router.push({ name: 'DeviceView', query: { id: rowData.id } });
  }

  function onDelete(record) {
    Modal.confirm({
      title: '提示',
      content: '确定要删除【' + record.name + '】吗?',
      okText: '删除',
      okType: 'danger',
      onOk() {
        singleDelete(record);
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  async function singleDelete(record) {
    try {
      SmartLoading.show();
      await deviceApi.delete(record.id);
      message.success('删除成功');
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }

  const selectedRowKeyList = ref([]);
  function onSelectChange(selectedRowKeys) {
    selectedRowKeyList.value = selectedRowKeys;
  }

  function confirmBatchDelete() {
    Modal.confirm({
      title: '提示',
      content: '确定要删除选中的设备吗?',
      okText: '删除',
      okType: 'danger',
      onOk() {
        batchDelete();
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  async function batchDelete() {
    try {
      SmartLoading.show();
      await deviceApi.batchDelete(selectedRowKeyList.value);
      message.success('删除成功');
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }
</script>
