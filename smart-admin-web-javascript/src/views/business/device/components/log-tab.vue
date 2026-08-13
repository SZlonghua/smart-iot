<template>
  <div>
    <a-form class="smart-query-form" layout="inline">
      <a-form-item label="类型">
        <SmartEnumSelect v-model:value="query.type" enum-name="DEVICE_LOG_TYPE_ENUM" width="150px" placeholder="日志类型" />
      </a-form-item>
      <a-form-item label="创建时间">
        <a-range-picker
          v-model:value="createTimeRange"
          valueFormat="YYYY-MM-DD HH:mm:ss"
          :placeholder="['开始时间', '结束时间']"
          @change="onCreateTimeChange"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" @click="onSearch">
          <template #icon><SearchOutlined /></template>查询
        </a-button>
        <a-button style="margin-left: 8px" @click="reset">
          <template #icon><ReloadOutlined /></template>重置
        </a-button>
      </a-form-item>
    </a-form>
    <a-table size="small" :columns="columns" :dataSource="data" :loading="loading" :pagination="false" rowKey="id" bordered style="margin-top: 8px">
      <template #bodyCell="{ text, column }">
        <template v-if="column.dataIndex === 'type'">
          {{ $smartEnumPlugin.getDescByValue('DEVICE_LOG_TYPE_ENUM', text) }}
        </template>
        <template v-if="column.dataIndex === 'content'">
          <a-typography-paragraph :ellipsis="{ rows: 2, expandable: true, symbol: '展开' }" style="margin-bottom: 0; max-width: 400px">
            {{ text }}
          </a-typography-paragraph>
        </template>
      </template>
    </a-table>
    <div class="smart-query-table-page">
      <a-pagination
        showSizeChanger
        showQuickJumper
        show-less-items
        :defaultPageSize="query.pageSize"
        v-model:current="query.pageNum"
        v-model:pageSize="query.pageSize"
        :total="total"
        @change="load"
        @showSizeChange="load"
        :show-total="(t) => `共${t}条`"
      />
    </div>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { deviceLogApi } from '/@/api/business/devicelog/device-log-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';

  const props = defineProps({
    deviceId: { type: Number, required: true },
  });

  const loading = ref(false);
  const data = ref([]);
  const total = ref(0);
  const defaultQuery = { type: undefined, createTimeBegin: undefined, createTimeEnd: undefined, pageNum: 1, pageSize: 10 };
  const query = reactive({ ...defaultQuery });
  const createTimeRange = ref(null);

  const columns = [
    { title: '设备ID', dataIndex: 'deviceId', resizable: true, width: 100 },
    { title: '设备名称', dataIndex: 'deviceName', resizable: true, width: 150 },
    { title: '日志类型', dataIndex: 'type', resizable: true, width: 120 },
    { title: '日志内容', dataIndex: 'content', resizable: true, width: 400 },
    { title: '创建时间', dataIndex: 'createTime', resizable: true, width: 170 },
  ];

  function onCreateTimeChange(dates) {
    if (dates && dates.length === 2) {
      query.createTimeBegin = dates[0];
      query.createTimeEnd = dates[1];
    } else {
      query.createTimeBegin = undefined;
      query.createTimeEnd = undefined;
    }
  }

  function onSearch() {
    query.pageNum = 1;
    load();
  }
  function reset() {
    let pageSize = query.pageSize;
    Object.assign(query, defaultQuery);
    query.pageSize = pageSize;
    createTimeRange.value = null;
    load();
  }

  async function load() {
    loading.value = true;
    try {
      const params = { ...query, deviceId: props.deviceId };
      if (!params.type) {
        delete params.type;
      }
      const res = await deviceLogApi.queryPage(params);
      data.value = res.data.list || [];
      total.value = res.data.total;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      loading.value = false;
    }
  }

  onMounted(load);
</script>
