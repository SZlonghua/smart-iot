<template>
  <a-modal
    :open="visible"
    :title="readonly ? '查看' : (form.id ? '编辑' : '添加')"
    :width="700"
    forceRender
    :ok-text="readonly ? '关闭' : '确认'"
    :cancel-text="readonly ? '' : '取消'"
    :ok-button-props="readonly ? {} : undefined"
    :cancel-button-props="readonly ? { style: { display: 'none' } } : undefined"
    @ok="onSubmit"
    @cancel="onClose"
  >
    <a-form ref="formRef" :model="form" :rules="readonly ? {} : rules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
      <a-form-item label="组件名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入组件名称" :disabled="readonly" />
      </a-form-item>

      <a-form-item label="组件类型" name="type">
        <SmartEnumSelect width="100%" v-model:value="form.type" placeholder="请选择组件类型" enum-name="COMPONENT_TYPE_ENUM" :disabled="readonly" @change="onTypeChange" />
      </a-form-item>

      <template v-if="form.type">
        <a-divider orientation="left" style="font-size: 13px; margin: 8px 0">
          {{ $smartEnumPlugin.getDescByValue('COMPONENT_TYPE_ENUM', form.type) }} 配置
        </a-divider>
        <NetworkConfigForm v-model="form.config" :type="form.type" :disabled="readonly" />
      </template>

      <a-divider style="margin: 8px 0" />

      <a-form-item label="启用状态" name="status">
        <a-radio-group v-model:value="form.status" :disabled="readonly">
          <a-radio :value="1">启用</a-radio>
          <a-radio :value="0">禁用</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item label="描述" name="description">
        <WangEditor v-if="!readonly" v-model="form.description" :height="300" :toolbar="false" />
        <div v-else class="rich-text-preview" v-html="form.description" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { message } from 'ant-design-vue';
import { nextTick, reactive, ref } from 'vue';
import { networkComponentApi } from '/@/api/business/networkcomponent/network-component-api';
import { SmartLoading } from '/@/components/framework/smart-loading';
import { smartSentry } from '/@/lib/smart-sentry';
import SmartEnumSelect from '/@/components/framework/smart-enum-select/index.vue';
import NetworkConfigForm from './NetworkConfigForm.vue';
import { COMPONENT_DEFAULT_CONFIG } from '/@/constants/business/networkcomponent/network-component-const';
import WangEditor from '/@/components/framework/wangeditor/index.vue';

defineExpose({ showModal });
const emit = defineEmits(['refresh']);

// --------------------- modal 显示与隐藏 ---------------------
const visible = ref(false);
const readonly = ref(false);

function showModal(rowData) {
  readonly.value = !!(rowData && rowData._readonly);
  Object.assign(form, formDefault);
  if (rowData && rowData.id) {
    let config = {};
    try {
      config = rowData.configuration ? JSON.parse(rowData.configuration) : {};
    } catch (e) {
      // configuration 不是合法 JSON，使用空对象
    }
    Object.assign(form, {
      id: rowData.id,
      name: rowData.name || '',
      type: rowData.type || '',
      config: config,
      status: rowData.status != null ? rowData.status : 1,
      description: rowData.description || '',
    });
  }
  visible.value = true;
  nextTick(() => {
    const domArr = document.getElementsByClassName('ant-modal');
    if (domArr && domArr.length > 0) {
      Array.from(domArr).forEach((item) => {
        if (item.childNodes && item.childNodes.length > 0) {
          Array.from(item.childNodes).forEach((child) => {
            if (child.setAttribute) {
              child.setAttribute('aria-hidden', 'false');
            }
          });
        }
      });
    }
  });
}

function onClose() {
  visible.value = false;
  readonly.value = false;
}

// --------------------- 类型切换 ---------------------
function onTypeChange(type) {
  if (COMPONENT_DEFAULT_CONFIG[type]) {
    form.config = JSON.parse(JSON.stringify(COMPONENT_DEFAULT_CONFIG[type]));
  } else {
    form.config = {};
  }
}

// --------------------- 表单 ---------------------
const formRef = ref();

const formDefault = {
  id: undefined,
  name: undefined,
  type: undefined,
  config: {},
  status: 1,
  description: undefined,
};
let form = reactive({ ...formDefault });

const rules = {
  name: [{ required: true, message: '请输入组件名称' }],
  type: [{ required: true, message: '请选择组件类型' }],
};

function onSubmit() {
  if (readonly.value) {
    onClose();
    return;
  }
  formRef.value
    .validate()
    .then(async () => {
      SmartLoading.show();
      try {
        const payload = {
          ...form,
          configuration: JSON.stringify(form.config),
        };
        if (payload.id) {
          await networkComponentApi.update(payload);
        } else {
          await networkComponentApi.add(payload);
        }
        message.success(`${form.id ? '修改' : '添加'}成功`);
        emit('refresh');
        onClose();
      } catch (error) {
        smartSentry.captureError(error);
      } finally {
        SmartLoading.hide();
      }
    })
    .catch((error) => {
      console.log('error', error);
      message.error('参数验证错误，请仔细填写表单数据!');
    });
}
</script>

<style lang="less" scoped>
:deep(.ant-card-body) {
  padding: 10px;
}
.rich-text-preview {
  min-height: 100px;
  padding: 8px 12px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background: #fafafa;
  :deep(img) {
    max-width: 100%;
  }
}
</style>
