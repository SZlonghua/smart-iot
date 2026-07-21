<template>
  <a-modal
    :open="visible"
    :title="form.id ? '编辑设备网关' : '添加设备网关'"
    :width="800"
    :body-style="{ minHeight: '420px' }"
    forceRender
    @cancel="onClose"
  >
    <a-steps :current="step - 1" size="small" style="margin-bottom: 24px">
      <a-step v-for="s in currentSteps" :key="s.key" :title="s.title" />
    </a-steps>

    <a-form ref="formRef" :model="form" :rules="currentRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 17 }">
      <!-- 接入类型 -->
      <template v-if="currentStepKey === 'type'">
        <a-form-item label="接入类型" name="type" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
          <div class="type-card-group">
            <div
              v-for="item in typeOptions"
              :key="item.value"
              :class="['type-card', { active: form.type === item.value, disabled: !!form.id }]"
              @click="onSelectType(item.value)"
            >
              <div class="type-card-title">{{ item.desc }}</div>
            </div>
          </div>
        </a-form-item>
      </template>

      <!-- 网络组件 -->
      <template v-if="currentStepKey === 'component'">
        <a-form-item label="搜索" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
          <a-input
            v-model:value="componentSearch"
            placeholder="搜索网络组件名称"
            allow-clear
            @pressEnter="
              componentPageNum = 1;
              loadComponentOptions();
            "
          />
        </a-form-item>
        <a-form-item label="网络组件" name="componentId" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
          <div v-if="componentOptions.length === 0" style="color: #999; text-align: center; padding: 40px">暂无可用网络组件</div>
          <div v-else class="type-card-group">
            <div
              v-for="item in componentOptions"
              :key="item.id"
              :class="['type-card', { active: form.componentId === item.id }]"
              @click="onSelectComponent(item)"
            >
              <div class="type-card-title">{{ item.name }}</div>
              <div class="type-card-sub">{{ item.type }}</div>
            </div>
          </div>
          <a-pagination
            v-if="componentTotal > componentPageSize"
            size="small"
            :current="componentPageNum"
            :pageSize="componentPageSize"
            :total="componentTotal"
            :show-size-changer="false"
            style="margin-top: 12px; text-align: right"
            @change="onComponentPageChange"
          />
        </a-form-item>
      </template>

      <!-- 消息协议 -->
      <template v-if="currentStepKey === 'protocol'">
        <a-form-item label="搜索" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
          <a-input
            v-model:value="protocolSearch"
            placeholder="搜索协议名称"
            allow-clear
            @pressEnter="
              protocolPageNum = 1;
              loadProtocolOptions();
            "
          />
        </a-form-item>
        <a-form-item label="消息协议" name="protocolId" :label-col="{ span: 3 }" :wrapper-col="{ span: 21 }">
          <div v-if="protocolOptions.length === 0" style="color: #999; text-align: center; padding: 40px">暂无可用协议</div>
          <div v-else class="type-card-group">
            <div
              v-for="item in protocolOptions"
              :key="item.id"
              :class="['type-card', { active: form.protocolId === item.id }]"
              @click="form.protocolId = item.id"
            >
              <div class="type-card-title">{{ item.name }}</div>
              <div class="type-card-sub">{{ item.version }}</div>
            </div>
          </div>
          <a-pagination
            v-if="protocolTotal > protocolPageSize"
            size="small"
            :current="protocolPageNum"
            :pageSize="protocolPageSize"
            :total="protocolTotal"
            :show-size-changer="false"
            style="margin-top: 12px; text-align: right"
            @change="onProtocolPageChange"
          />
        </a-form-item>
      </template>

      <!-- 完成 -->
      <template v-if="currentStepKey === 'finish'">
        <a-form-item label="网关名称" name="name">
          <a-input v-model:value="form.name" placeholder="请输入网关名称" />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea v-model:value="form.description" placeholder="请输入描述" :rows="5" />
        </a-form-item>
      </template>
    </a-form>
    <template #footer>
      <a-space>
        <a-button v-if="step > 1" @click="onPrev">上一步</a-button>
        <a-button @click="onClose">{{ step === 1 ? '取消' : '关闭' }}</a-button>
        <a-button type="primary" @click="onNext">{{ step === maxStep ? '确认' : '下一步' }}</a-button>
      </a-space>
    </template>
  </a-modal>
</template>

<script setup>
  import { message } from 'ant-design-vue';
  import { reactive, ref, computed, nextTick } from 'vue';
  import { gatewayApi } from '/@/api/business/gateway/gateway-api';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { smartSentry } from '/@/lib/smart-sentry';
  import { ACCESS_TYPE_ENUM, ACCESS_TYPE_COMPONENT_MAP, COMPONENT_TYPE_TRANSPORT_MAP } from '/@/constants/business/gateway/gateway-const';

  // 每种接入类型的步骤定义（后续可按类型独立扩展）
  const TYPE_STEPS = {
    'tcp': [
      { key: 'type', title: '接入类型' },
      { key: 'component', title: '网络组件' },
      { key: 'protocol', title: '消息协议' },
      { key: 'finish', title: '完成' },
    ],
    'http': [
      { key: 'type', title: '接入类型' },
      { key: 'component', title: '网络组件' },
      { key: 'protocol', title: '消息协议' },
      { key: 'finish', title: '完成' },
    ],
    'mqtt-server': [
      { key: 'type', title: '接入类型' },
      { key: 'component', title: '网络组件' },
      { key: 'protocol', title: '消息协议' },
      { key: 'finish', title: '完成' },
    ],
    'mqtt-broker': [
      { key: 'type', title: '接入类型' },
      { key: 'component', title: '网络组件' },
      { key: 'protocol', title: '消息协议' },
      { key: 'finish', title: '完成' },
    ],
    'tcp_transparent': [
      { key: 'type', title: '接入类型' },
      { key: 'component', title: '网络组件' },
      { key: 'protocol', title: '消息协议' },
      { key: 'finish', title: '完成' },
    ],
    'gateway_child': [
      { key: 'type', title: '接入类型' },
      { key: 'protocol', title: '消息协议' },
      { key: 'finish', title: '完成' },
    ],
  };

  defineExpose({ showModal });
  const emit = defineEmits(['refresh']);

  // --------------------- 步骤控制 ---------------------
  const step = ref(1);
  // 未选类型时用 TCP 的步骤占位（maxStep > 1 避免按钮直接变"确认"）
  const currentSteps = computed(() => TYPE_STEPS[form.type] || TYPE_STEPS['tcp']);
  const maxStep = computed(() => currentSteps.value.length);
  const currentStepKey = computed(() => currentSteps.value[step.value - 1]?.key);

  // --------------------- modal ---------------------
  const visible = ref(false);

  function showModal(rowData) {
    step.value = 1;
    Object.assign(form, formDefault);
    if (rowData && rowData.id) {
      Object.assign(form, {
        id: rowData.id,
        name: rowData.name || '',
        type: rowData.type || '',
        componentId: rowData.componentId || null,
        protocolId: rowData.protocolId || null,
        transport: rowData.transport || '',
        description: rowData.description || '',
      });
      componentPageNum.value = 1;
      componentSearch.value = '';
      protocolPageNum.value = 1;
      protocolSearch.value = '';
      loadComponentOptions();
      loadProtocolOptions();
    }
    visible.value = true;
    nextTick(() => formRef.value?.clearValidate());
  }

  function onClose() {
    visible.value = false;
  }

  // 接入类型卡片选项
  const typeOptions = Object.values(ACCESS_TYPE_ENUM);

  function onSelectType(value) {
    if (form.id) return;
    form.type = value;
    onTypeChange(value);
  }

  // --------------------- 卡片数据 ---------------------
  const componentOptions = ref([]);
  const componentPageNum = ref(1);
  const componentPageSize = 6;
  const componentTotal = ref(0);
  const componentSearch = ref('');

  const protocolOptions = ref([]);
  const protocolPageNum = ref(1);
  const protocolPageSize = 6;
  const protocolTotal = ref(0);
  const protocolSearch = ref('');

  async function loadComponentOptions() {
    const ct = ACCESS_TYPE_COMPONENT_MAP[form.type];
    if (!ct) return;
    try {
      const res = await gatewayApi.queryNetworkComponents({
        type: ct,
        name: componentSearch.value || undefined,
        pageNum: componentPageNum.value,
        pageSize: componentPageSize,
      });
      componentOptions.value = res.data.list || [];
      componentTotal.value = res.data.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  function onComponentPageChange(page) {
    componentPageNum.value = page;
    loadComponentOptions();
  }

  function onSelectComponent(comp) {
    form.componentId = comp.id;
    form.transport = COMPONENT_TYPE_TRANSPORT_MAP[comp.type] || '';
  }

  async function loadProtocolOptions() {
    try {
      const res = await gatewayApi.queryProtocols({
        name: protocolSearch.value || undefined,
        pageNum: protocolPageNum.value,
        pageSize: protocolPageSize,
      });
      protocolOptions.value = res.data.list || [];
      protocolTotal.value = res.data.total || 0;
    } catch (e) {
      smartSentry.captureError(e);
    }
  }

  function onProtocolPageChange(page) {
    protocolPageNum.value = page;
    loadProtocolOptions();
  }

  // --------------------- 类型切换 ---------------------
  async function onTypeChange(type) {
    if (form.id) return;
    form.componentId = null;
    form.transport = '';
    componentPageNum.value = 1;
    componentSearch.value = '';
    if (ACCESS_TYPE_COMPONENT_MAP[type] === null) {
      form.transport = 'Gateway';
    } else {
      await loadComponentOptions();
    }
  }

  // --------------------- 表单 ---------------------
  const formRef = ref();
  const formDefault = { id: undefined, name: undefined, type: undefined, componentId: null, protocolId: null, transport: '', description: undefined };
  let form = reactive({ ...formDefault });

  const rules = {
    type: [{ required: true, message: '请选择接入类型' }],
    componentId: [{ required: true, message: '请选择网络组件' }],
    protocolId: [{ required: true, message: '请选择消息协议' }],
    name: [{ required: true, message: '请输入网关名称' }],
  };

  const currentRules = computed(() => {
    const key = currentStepKey.value;
    if (key === 'type') return { type: rules.type };
    if (key === 'component') return { componentId: rules.componentId };
    if (key === 'protocol') return { protocolId: rules.protocolId };
    return { name: rules.name };
  });

  // --------------------- 步骤导航 ---------------------
  async function onNext() {
    // 未选类型不能进入下一步
    if (step.value === 1 && !form.type) {
      message.warning('请先选择接入类型');
      return;
    }

    if (step.value < maxStep.value) {
      try {
        await formRef.value.validate();
        step.value++;
        if (currentStepKey.value === 'component') await loadComponentOptions();
        if (currentStepKey.value === 'protocol') await loadProtocolOptions();
        nextTick(() => formRef.value?.clearValidate());
      } catch (e) {
        /* 校验失败 */
      }
      return;
    }

    try {
      await formRef.value.validate();
    } catch (e) {
      message.error('参数验证错误，请仔细填写表单数据!');
      return;
    }
    SmartLoading.show();
    try {
      const payload = { ...form };
      if (payload.type === 'gateway_child') {
        payload.componentId = null;
        payload.transport = 'Gateway';
      }
      if (payload.id) await gatewayApi.update(payload);
      else await gatewayApi.add(payload);
      message.success(`${form.id ? '修改' : '添加'}成功`);
      emit('refresh');
      onClose();
    } catch (e) {
      smartSentry.captureError(e);
    } finally {
      SmartLoading.hide();
    }
  }

  function onPrev() {
    if (step.value > 1) { step.value--; nextTick(() => formRef.value?.clearValidate()); return; }
    onClose();
  }
</script>

<style lang="less" scoped>
  :deep(.ant-card-body) {
    padding: 10px;
  }
  .type-card-group {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  .type-card {
    flex: 0 0 calc(50% - 8px);
    padding: 24px 16px 20px;
    border: 1px solid #d9d9d9;
    border-radius: 8px;
    text-align: center;
    cursor: pointer;
    transition: all 0.25s ease;
    background: #fff;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
    &:hover:not(.disabled) {
      border-color: #1890ff;
      box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
      transform: translateY(-2px);
    }
    &.active {
      border-color: #1890ff;
      background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
      box-shadow: 0 2px 8px rgba(24, 144, 255, 0.12);
      .type-card-title {
        color: #1890ff;
        font-weight: 600;
      }
      .type-card-sub {
        color: #69c0ff;
      }
    }
    &.disabled {
      cursor: not-allowed;
      opacity: 0.6;
      &:hover {
        transform: none;
        box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
      }
    }
    .type-card-title {
      font-size: 15px;
      font-weight: 500;
      color: #333;
      margin-bottom: 6px;
    }
    .type-card-sub {
      font-size: 12px;
      color: #bbb;
    }
  }
</style>
