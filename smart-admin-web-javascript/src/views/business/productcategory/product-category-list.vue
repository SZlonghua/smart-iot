<template>
  <!---------- 查询表单 begin ---------->
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row">
      <a-form-item label="分类名称" class="smart-query-form-item">
        <a-input style="width: 200px" @pressEnter="onSearch" v-model:value="queryForm.name" placeholder="分类名称" />
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
          <template #icon><PlusOutlined /></template>新建分类
        </a-button>
        <a-button @click="confirmBatchDelete" type="primary" danger :disabled="selectedRowKeyList.length == 0">
          <template #icon><DeleteOutlined /></template>批量删除
        </a-button>
      </div>
      <div class="smart-table-setting-block">
        <TableOperator v-model="columns" :tableId="TABLE_ID_CONST.BUSINESS.IOT.PRODUCT_CATEGORY" :refresh="queryData" />
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
      :defaultExpandAllRows="false"
      :expandedRowKeys="expandedRowKeys"
      :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }"
      @expand="onExpand"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button @click="showAddChild(record)" type="link">添加子分类</a-button>
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

    <ProductCategoryForm ref="formRef" @reloadList="handleReload" />
  </a-card>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { productCategoryApi } from '/@/api/business/productcategory/product-category-api';
  import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { TABLE_ID_CONST } from '/@/constants/support/table-id-const';
  import TableOperator from '/@/components/support/table-operator/index.vue';
  import ProductCategoryForm from './product-category-form-modal.vue';
  import _ from 'lodash';

  // 表格列
  const columns = ref([
    { title: '分类名称', dataIndex: 'name', resizable: true, width: 250 },
    { title: '排序值', dataIndex: 'sortOrder', resizable: true, width: 100 },
    { title: '描述', dataIndex: 'description', resizable: true, width: 200 },
    { title: '创建时间', dataIndex: 'createTime', resizable: true, width: 170 },
    { title: '操作', dataIndex: 'action', fixed: 'right', width: 210 },
  ]);

  // 查询表单
  const queryFormState = {
    name: '',
    parentId: 0,
    pageNum: 1,
    pageSize: 10,
  };
  const queryForm = reactive({ ...queryFormState });
  // 表格加载
  const tableLoading = ref(false);
  // 表格数据
  const tableData = ref([]);
  // 总数
  const total = ref(0);
  // 当前展开的行
  const expandedRowKeys = ref([]);

  function resetQuery() {
    Object.assign(queryForm, queryFormState, { pageSize: queryForm.pageSize });
    queryData();
  }

  function onSearch() {
    queryForm.pageNum = 1;
    queryData();
  }

  // 查询数据
  async function queryData() {
    tableLoading.value = true;
    try {
      let queryResult = await productCategoryApi.queryPage({ ...queryForm });
      tableData.value = (queryResult.data.list || []).map((item) => {
        const node = { ...item };
        if (node.hasChildren) {
          node.children = [];
        }
        return node;
      });
      total.value = queryResult.data.total;

      expandedRowKeys.value = [];
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      tableLoading.value = false;
    }
  }

  // 递归查找节点
  function findNode(nodes, id) {
    for (const node of nodes) {
      if (node.id === id) return node;
      if (node.children && node.children.length > 0) {
        const found = findNode(node.children, id);
        if (found) return found;
      }
    }
    return null;
  }

  // 获取子节点
  async function fetchChildren(record) {
    try {
      const res = await productCategoryApi.queryChildren(record.id);
      const childrenList = (res.data || []).map((item) => {
        const node = { ...item };
        if (node.hasChildren) {
          node.children = [];
        }
        return node;
      });
      if (childrenList.length > 0) {
        record.children = childrenList;
      } else {
        delete record.children;
      }
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  // 表单提交后刷新
  async function handleReload(parentId) {
    if (parentId > 0) {
      // 子节点操作：在当前已加载的树中找父节点，刷新其子节点
      const parent = findNode(tableData.value, parentId);
      if (parent) {
        await fetchChildren(parent);
        if (!expandedRowKeys.value.includes(parentId)) {
          expandedRowKeys.value = [...expandedRowKeys.value, parentId];
        }
        return;
      }
    }
    // 根节点操作或父节点不在当前页：全量刷新
    await queryData();
  }

  // 展开时懒加载子节点
  async function onExpand(expanded, record) {
    if (expanded) {
      // children 为空数组 = 有子节点但未加载
      if (record.children && record.children.length === 0) {
        await fetchChildren(record);
      }
      expandedRowKeys.value = [...expandedRowKeys.value, record.id];
    } else {
      expandedRowKeys.value = expandedRowKeys.value.filter((k) => k !== record.id);
    }
  }

  onMounted(queryData);

  const formRef = ref();
  function showForm(rowData) {
    formRef.value.showDrawer(rowData);
  }

  function showAddChild(rowData) {
    formRef.value.showDrawer({ parentId: rowData.id });
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
      await productCategoryApi.delete(record.id);
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
