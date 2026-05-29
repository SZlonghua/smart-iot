<template>
  <!---------- 查询表单 begin ---------->
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row">
      <a-form-item label="分类名称" class="smart-query-form-item">
        <a-input style="width: 200px" v-model:value="queryForm.name" placeholder="分类名称" />
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
        <a-button @click="showForm" type="primary" size="small">
          <template #icon><PlusOutlined /></template>新建
        </a-button>
        <a-button @click="confirmBatchDelete" type="primary" danger size="small" :disabled="selectedRowKeyList.length == 0">
          <template #icon><DeleteOutlined /></template>批量删除
        </a-button>
      </div>
      <div class="smart-table-setting-block">
        <TableOperator v-model="columns" :tableId="null" :refresh="queryData" />
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
        <template v-if="column.dataIndex === 'parentId'">
          <span>{{ text === 0 ? '顶级分类' : text }}</span>
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button @click="showForm(record)" type="link">编辑</a-button>
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

    <ProductCategoryForm ref="formRef" @reloadList="queryData" />
  </a-card>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { productCategoryApi } from '/@/api/business/productcategory/product-category-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import ProductCategoryForm from './product-category-form-modal.vue';
  import _ from 'lodash';

  // 表格列
  const columns = ref([
    { title: '分类名称', dataIndex: 'name', resizable: true, width: 200 },
    { title: '父分类ID', dataIndex: 'parentId', resizable: true, width: 120 },
    { title: '排序值', dataIndex: 'sortOrder', resizable: true, width: 100 },
    { title: '描述', dataIndex: 'description', resizable: true, width: 200 },
    { title: '创建时间', dataIndex: 'createTime', resizable: true, width: 170 },
    { title: '操作', dataIndex: 'action', fixed: 'right', width: 150 },
  ]);

  // 查询数据表单和方法
  const queryFormState = {
    name: '',
    pageNum: 1,
    pageSize: 10,
    sortItemList: [],
  };
  const queryForm = reactive(_.cloneDeep(queryFormState));
  // 表格加载loading
  const tableLoading = ref(false);
  // 表格数据
  const tableData = ref([]);
  // 总数
  const total = ref(0);

  // 重置查询条件
  function resetQuery() {
    let pageSize = queryForm.pageSize;
    Object.assign(queryForm, _.cloneDeep(queryFormState));
    queryForm.pageSize = pageSize;
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
      let queryResult = await productCategoryApi.queryPage(queryForm);
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
  // 添加/修改
  function showForm(rowData) {
    formRef.value.showDrawer(rowData);
  }

  // 单个删除
  function onDelete(record) {
    Modal.confirm({
      title: '提示',
      content: '确定要删除【' + record.name + '】吗?',
      okText: '删除',
      okType: 'danger',
      // 确认删除
      onOk() {
        singleDelete(record);
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  // 请求删除
  async function singleDelete(record) {
    try {
      SmartLoading.show();
      await productCategoryApi.delete(record.id);
      message.success('删除成功');
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }

  // 选择表格行
  const selectedRowKeyList = ref([]);
  function onSelectChange(selectedRowKeys) {
    selectedRowKeyList.value = selectedRowKeys;
  }

  // 批量删除
  function confirmBatchDelete() {
    Modal.confirm({
      title: '提示',
      content: '确定要删除选中的分类吗?',
      okText: '删除',
      okType: 'danger',
      onOk() {
        batchDelete();
      },
      cancelText: '取消',
      onCancel() {},
    });
  }

  // 请求批量删除
  async function batchDelete() {
    try {
      SmartLoading.show();
      await productCategoryApi.batchDelete(selectedRowKeyList.value);
      message.success('删除成功');
      queryData();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }
</script>
