<template>
  <!---------- 查询表单 begin ---------->
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row">
      <a-form-item label="设备Key" class="smart-query-form-item">
        <a-input style="width: 200px" @pressEnter="onSearch" v-model:value="queryForm.deviceKey" placeholder="设备Key" />
      </a-form-item>
      <a-form-item label="告警级别" class="smart-query-form-item">
        <SmartEnumSelect @pressEnter="onSearch" v-model:value="queryForm.level" enum-name="ALARM_LEVEL_ENUM" width="160px" placeholder="告警级别" />
      </a-form-item>
      <a-form-item label="处理状态" class="smart-query-form-item">
        <SmartEnumSelect @pressEnter="onSearch" v-model:value="queryForm.status" enum-name="ALARM_STATUS_ENUM" width="160px" placeholder="处理状态" />
      </a-form-item>
      <a-form-item label="触发时间" class="smart-query-form-item">
        <a-range-picker
          v-model:value="triggerTimeRange"
          valueFormat="YYYY-MM-DD HH:mm:ss"
          :placeholder="['开始时间', '结束时间']"
          @change="onTriggerTimeChange"
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
        <template v-if="column.dataIndex === 'level'">
          <span>{{ $smartEnumPlugin.getDescByValue('ALARM_LEVEL_ENUM', text) }}</span>
        </template>
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="getStatusColor(text)">
            {{ $smartEnumPlugin.getDescByValue('ALARM_STATUS_ENUM', text) }}
          </a-tag>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button v-if="$privilege('alarmLog:handle')" @click="showHandleModal(record)" type="link">处理</a-button>
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

    <AlarmLogHandleModal ref="handleModalRef" @reloadList="queryData" />
  </a-card>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { alarmLogApi } from '/@/api/business/alarmlog/alarm-log-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
  import AlarmLogHandleModal from './alarm-log-handle-modal.vue';
  import _ from 'lodash';

  // 表格列
  const columns = ref([
    { title: '设备Key', dataIndex: 'deviceKey', resizable: true, width: 150 },
    { title: '告警级别', dataIndex: 'level', resizable: true, width: 100 },
    { title: '告警描述', dataIndex: 'description', resizable: true, width: 200 },
    { title: '处理状态', dataIndex: 'status', resizable: true, width: 100 },
    { title: '触发时间', dataIndex: 'triggerTime', resizable: true, width: 170 },
    { title: '处理时间', dataIndex: 'handleTime', resizable: true, width: 170 },
    { title: '处理人', dataIndex: 'handler', resizable: true, width: 100 },
    { title: '处理备注', dataIndex: 'handleNote', resizable: true, width: 150 },
    { title: '操作', dataIndex: 'action', fixed: 'right', width: 80 },
  ]);

  // 查询数据表单和方法
  const queryFormState = {
    deviceKey: '',
    level: undefined,
    status: undefined,
    triggerTimeBegin: undefined,
    triggerTimeEnd: undefined,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  const triggerTimeRange = ref(null);
  // 表格加载loading
  const tableLoading = ref(false);
  // 表格数据
  const tableData = ref([]);
  // 总数
  const total = ref(0);

  function onTriggerTimeChange(dates) {
    if (dates && dates.length === 2) {
      queryForm.triggerTimeBegin = dates[0];
      queryForm.triggerTimeEnd = dates[1];
    } else {
      queryForm.triggerTimeBegin = undefined;
      queryForm.triggerTimeEnd = undefined;
    }
  }

  function getStatusColor(status) {
    const colorMap = {
      1: 'red',
      2: 'orange',
      3: 'green',
      4: 'gray',
    };
    return colorMap[status] || 'default';
  }

  // 重置查询条件
  function resetQuery() {
    let pageSize = queryForm.pageSize;
    Object.assign(queryForm, queryFormState);
    queryForm.pageSize = pageSize;
    triggerTimeRange.value = null;
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
      let queryResult = await alarmLogApi.queryPage({ ...queryForm });
      tableData.value = queryResult.data.list;
      total.value = queryResult.data.total;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  onMounted(queryData);

  const handleModalRef = ref();
  function showHandleModal(record) {
    handleModalRef.value.show(record);
  }
</script>
