<!--
  * 设备网关列表
  *
  * @Author:    1024创新实验室
  * @Date:      2026-06-01
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <!-- 查询表单 begin -->
  <a-form class="smart-query-form" v-privilege="'gateway:query'">
    <a-row class="smart-query-form-row">
      <a-form-item label="网关名称" class="smart-query-form-item">
        <a-input style="width: 200px" @pressEnter="onSearch" v-model:value="queryForm.name" placeholder="网关名称" />
      </a-form-item>

      <a-form-item label="接入类型" class="smart-query-form-item">
        <SmartEnumSelect
          style="width: 200px"
          v-model:value="queryForm.type"
          placeholder="所有类型"
          enum-name="ACCESS_TYPE_ENUM"
          :allow-clear="true"
        />
      </a-form-item>

      <a-form-item class="smart-query-form-item smart-margin-left10">
        <a-button-group>
          <a-button type="primary" @click="onSearch">
            <template #icon><SearchOutlined /></template>查询
          </a-button>
          <a-button @click="resetQuery">
            <template #icon><ReloadOutlined /></template>重置
          </a-button>
        </a-button-group>
      </a-form-item>
    </a-row>
  </a-form>
  <!-- 查询表单 end -->

  <a-card size="small" :bordered="false" :hoverable="true">
    <a-row class="smart-table-btn-block">
      <div class="smart-table-operate-block">
        <a-button @click="add()" v-privilege="'gateway:add'" type="primary">
          <template #icon><PlusOutlined /></template>新建网关
        </a-button>
      </div>
    </a-row>

    <!-- 表格 begin -->
    <a-table
      :scroll="{ x: 1000 }"
      size="small"
      :dataSource="tableData"
      :columns="columns"
      rowKey="id"
      :pagination="false"
      :loading="tableLoading"
      bordered
    >
      <template #bodyCell="{ column, record, text }">
        <template v-if="column.dataIndex === 'type'">
          <span>{{ $smartEnumPlugin.getDescByValue('ACCESS_TYPE_ENUM', text) }}</span>
        </template>
        <template v-if="column.dataIndex === 'status'">
          <a-tag :color="text === 1 ? 'green' : 'red'">{{ text === 1 ? '启用' : '禁用' }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'componentName'">
          <span>{{ record.type === 'gateway_child' ? '其他网关设备接入' : text || '-' }}</span>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button @click="viewDetail(record)" size="small" type="link">查看</a-button>
            <a-button
              @click="update(record)"
              size="small"
              v-privilege="'gateway:update'"
              type="link"
              :disabled="record.status === 1"
              :title="record.status === 1 ? '请先禁用再编辑' : ''"
              >编辑</a-button
            >
            <a-button @click="toggleStatus(record)" size="small" type="link">{{ record.status === 1 ? '禁用' : '启用' }}</a-button>
            <a-button @click="confirmDelete(record.id)" size="small" danger v-privilege="'gateway:delete'" type="link">删除</a-button>
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
        @change="ajaxQuery"
        :show-total="(total) => `共${total}条`"
      />
    </div>
    <GatewayOperate ref="operateRef" @refresh="ajaxQuery" />
    <GatewayDetail ref="detailRef" />
  </a-card>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { gatewayApi } from '/@/api/business/gateway/gateway-api';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import GatewayOperate from './components/gateway-form-modal.vue';
  import GatewayDetail from './components/GatewayDetail.vue';
  import { smartSentry } from '/@/lib/smart-sentry';
  import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';

  // 表格列
  const columns = ref([
    { title: '设备网关名称', dataIndex: 'name', minWidth: 150, ellipsis: true },
    { title: '接入类型', dataIndex: 'type', width: 150 },
    { title: '状态', dataIndex: 'status', width: 80 },
    { title: '协议名称', dataIndex: 'protocolName', width: 150, ellipsis: true },
    { title: '网络组件名称', dataIndex: 'componentName', width: 180, ellipsis: true },
    { title: '描述', dataIndex: 'description', minWidth: 150, ellipsis: true },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    { title: '操作', dataIndex: 'action', width: 240 },
  ]);

  // 查询
  const queryFormState = { name: '', type: undefined, pageNum: 1, pageSize: PAGE_SIZE };
  const queryForm = reactive({ ...queryFormState });
  const tableLoading = ref(false);
  const tableData = ref([]);
  const total = ref(0);

  async function toggleStatus(record) {
    try {
      SmartLoading.show();
      const newStatus = record.status === 1 ? 0 : 1;
      await gatewayApi.updateStatus(record.id, newStatus);
      message.success(newStatus === 1 ? '已启用' : '已禁用');
      await ajaxQuery();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }

  function onSearch() {
    queryForm.pageNum = 1;
    ajaxQuery();
  }

  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    ajaxQuery();
  }

  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let res = await gatewayApi.queryPage(queryForm);
      tableData.value = res.data.list;
      total.value = res.data.total;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // 删除
  function confirmDelete(id) {
    Modal.confirm({
      title: '确定要删除吗？',
      content: '删除后，该信息将不可恢复',
      okText: '删除',
      okType: 'danger',
      onOk() {
        del(id);
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  async function del(id) {
    try {
      SmartLoading.show();
      await gatewayApi.delete(id);
      message.success('删除成功');
      ajaxQuery();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }

  // 添加/修改/查看
  const operateRef = ref();
  function add() {
    operateRef.value.showModal();
  }
  function update(record) {
    operateRef.value.showModal(record);
  }

  const detailRef = ref();
  function viewDetail(record) {
    detailRef.value.show({ id: record.id });
  }

  onMounted(ajaxQuery);
</script>
