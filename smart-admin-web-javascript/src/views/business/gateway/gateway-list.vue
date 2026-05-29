<!--
  * 设备网关列表
  *
  * @Author:    1024创新实验室
  * @Date:      2026-05-26
  * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
-->
<template>
  <!-- 查询表单 begin -->
  <a-form class="smart-query-form" v-privilege="'gateway:query'">
    <a-row class="smart-query-form-row">
      <a-form-item label="网关名称" class="smart-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.name" placeholder="网关名称" />
      </a-form-item>

      <a-form-item label="网关类型" class="smart-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.type" placeholder="网关类型" />
      </a-form-item>

      <a-form-item label="传输方式" class="smart-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.transport" placeholder="传输方式" />
      </a-form-item>

      <a-form-item class="smart-query-form-item smart-margin-left10">
        <a-button-group>
          <a-button type="primary" @click="onSearch">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
          <a-button @click="resetQuery">
            <template #icon><ReloadOutlined /></template>
            重置
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
          <template #icon><PlusOutlined /></template>
          新建
        </a-button>
      </div>
    </a-row>

    <!-- 表格 begin -->
    <a-table
      :scroll="{ x: 1200 }"
      size="small"
      :dataSource="tableData"
      :columns="columns"
      rowKey="id"
      :pagination="false"
      :loading="tableLoading"
      bordered
    >
      <template #bodyCell="{ column, record, text }">
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button @click="update(record.id)" size="small" v-privilege="'gateway:update'" type="link">编辑</a-button>
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
  </a-card>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { gatewayApi } from '/@/api/business/gateway/gateway-api';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import GatewayOperate from './components/gateway-form-modal.vue';
  import { smartSentry } from '/@/lib/smart-sentry';

  // --------------------------- 表格列 ---------------------------

  // 表格列
  const columns = ref([
    {
      title: '网关名称',
      dataIndex: 'name',
      minWidth: 150,
      ellipsis: true,
    },
    {
      title: '网关类型',
      dataIndex: 'type',
      width: 120,
    },
    {
      title: '网络组件',
      dataIndex: 'componentName',
      width: 150,
      ellipsis: true,
    },
    {
      title: '协议',
      dataIndex: 'protocolName',
      width: 120,
      ellipsis: true,
    },
    {
      title: '传输方式',
      dataIndex: 'transport',
      width: 100,
    },
    {
      title: '描述',
      dataIndex: 'description',
      minWidth: 150,
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      width: 170,
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 120,
    },
  ]);

  // --------------------------- 查询 ---------------------------

  // 查询数据表单和方法
  const queryFormState = {
    name: '',
    type: '',
    transport: '',
    pageNum: 1,
    pageSize: PAGE_SIZE,
  };
  const queryForm = reactive({ ...queryFormState });
  // 表格加载loading
  const tableLoading = ref(false);
  // 表格数据
  const tableData = ref([]);
  // 总数
  const total = ref(0);

  // 搜索
  function onSearch() {
    queryForm.pageNum = 1;
    ajaxQuery();
  }

  // 重置查询条件
  function resetQuery() {
    Object.assign(queryForm, queryFormState);
    ajaxQuery();
  }

  // 查询数据
  async function ajaxQuery() {
    try {
      tableLoading.value = true;
      let responseModel = await gatewayApi.queryPage(queryForm);
      const list = responseModel.data.list;
      total.value = responseModel.data.total;
      tableData.value = list;
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // --------------------------- 删除 ---------------------------

  // 单个删除
  function confirmDelete(id) {
    Modal.confirm({
      title: '确定要删除吗？',
      content: '删除后，该信息将不可恢复',
      okText: '删除',
      okType: 'danger',
      // 确认删除
      onOk() {
        del(id);
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  // 请求删除
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

  // --------------------------- 增加、修改 ---------------------------

  // 添加/修改
  const operateRef = ref();
  function add() {
    operateRef.value.showModal();
  }

  function update(id) {
    operateRef.value.showModal(id);
  }

  onMounted(ajaxQuery);
</script>
