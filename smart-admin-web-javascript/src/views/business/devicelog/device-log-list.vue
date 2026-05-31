<template>
  <!---------- 查询表单 begin ---------->
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row">
      <a-form-item label="设备名称" class="smart-query-form-item">
        <a-input style="width: 200px" @pressEnter="onSearch" v-model:value="queryForm.deviceName" placeholder="设备名称" />
      </a-form-item>
      <a-form-item label="日志类型" class="smart-query-form-item">
        <a-input style="width: 200px" @pressEnter="onSearch" v-model:value="queryForm.type" placeholder="日志类型" />
      </a-form-item>
      <a-form-item label="创建时间" class="smart-query-form-item">
        <a-range-picker
          v-model:value="createTimeRange"
          valueFormat="YYYY-MM-DD HH:mm:ss"
          :placeholder="['开始时间', '结束时间']"
          @change="onCreateTimeChange"
        />
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
    >
      <template #bodyCell="{ text, record, column }">
        <template v-if="column.dataIndex === 'content'">
          <a-typography-paragraph :ellipsis="{ rows: 2, expandable: true, symbol: '展开' }" style="margin-bottom: 0; max-width: 400px">
            {{ text }}
          </a-typography-paragraph>
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
  </a-card>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { deviceLogApi } from '/@/api/business/devicelog/device-log-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import _ from 'lodash';

  // 表格列
  const columns = ref([
    { title: '设备ID', dataIndex: 'deviceId', resizable: true, width: 100 },
    { title: '设备名称', dataIndex: 'deviceName', resizable: true, width: 150 },
    { title: '日志类型', dataIndex: 'type', resizable: true, width: 120 },
    { title: '日志内容', dataIndex: 'content', resizable: true, width: 400 },
    { title: '创建时间', dataIndex: 'createTime', resizable: true, width: 170 },
  ]);

  // 查询数据表单和方法
  const queryFormState = {
    deviceName: '',
    type: '',
    deviceId: undefined,
    createTimeBegin: undefined,
    createTimeEnd: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const createTimeRange = ref(null);
  // 表格加载loading
  const tableLoading = ref(false);
  // 表格数据
  const tableData = ref([]);
  // 总数
  const total = ref(0);

  function onCreateTimeChange(dates) {
    if (dates && dates.length === 2) {
      queryForm.createTimeBegin = dates[0];
      queryForm.createTimeEnd = dates[1];
    } else {
      queryForm.createTimeBegin = undefined;
      queryForm.createTimeEnd = undefined;
    }
  }

  // 重置查询条件
  function resetQuery() {
    let pageSize = queryForm.pageSize;
    Object.assign(queryForm, queryFormState);
    queryForm.pageSize = pageSize;
    createTimeRange.value = null;
    queryData();
  }

  // 搜索
  function onSearch() {
    queryForm.pageNum = 1;
    queryData();
  }

  // 查询数据
  async function queryData() {
    tableLoading.value = true;
    try {
      let queryResult = await deviceLogApi.queryPage({ ...queryForm });
      tableData.value = queryResult.data.list;
      total.value = queryResult.data.total;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  onMounted(queryData);
</script>
