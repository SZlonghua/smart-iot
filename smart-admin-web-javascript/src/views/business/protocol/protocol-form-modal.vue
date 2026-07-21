<template>
  <a-drawer :title="form.id ? '编辑' : '添加'" :width="500" :open="visible" :body-style="{ paddingBottom: '80px' }" @close="onClose">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      <a-form-item label="协议名称" name="name">
        <a-input v-model:value="form.name" placeholder="请输入协议名称" />
      </a-form-item>
      <a-form-item label="版本号" name="version">
        <a-input v-model:value="form.version" placeholder="请输入版本号" />
      </a-form-item>
      <a-form-item label="JAR包" name="jarPath">
        <a-upload
          v-model:file-list="jarFileList"
          :max-count="1"
          accept=".jar"
          :customRequest="customUpload"
        >
          <a-button>
            <upload-outlined />
            上传JAR包
          </a-button>
        </a-upload>
      </a-form-item>
      <a-form-item label="协议描述" name="description">
        <a-input v-model:value="form.description" placeholder="请输入协议描述" />
      </a-form-item>
    </a-form>
    <div
      :style="{
        position: 'absolute',
        right: 0,
        bottom: 0,
        width: '100%',
        borderTop: '1px solid #e9e9e9',
        padding: '10px 16px',
        background: '#fff',
        textAlign: 'right',
        zIndex: 1,
      }"
    >
      <a-button style="margin-right: 8px" @click="onClose">取消</a-button>
      <a-button type="primary" @click="onSubmit">提交</a-button>
    </div>
  </a-drawer>
</template>

<script setup>
  import { ref, nextTick, reactive, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { UploadOutlined } from '@ant-design/icons-vue';
  import { SmartLoading } from '/@/components/framework/smart-loading';
  import { protocolApi } from '/@/api/business/protocol/protocol-api';
  import { fileApi } from '/@/api/support/file-api';
  import { FILE_FOLDER_TYPE_ENUM } from '/@/constants/support/file-const';
  import { smartSentry } from '/@/lib/smart-sentry';
  import _ from 'lodash';

  const emit = defineEmits(['reloadList']);
  const formRef = ref();

  const formDefault = {
    id: undefined,
    name: '',
    version: '',
    jarPath: '',
    jarName: '',
    description: '',
  };
  let form = reactive({ ...formDefault });

  const rules = {
    name: [{ required: true, message: '协议名称不能为空' }],
    version: [{ required: true, message: '版本号不能为空' }],
    jarPath: [{ required: true, message: '请上传JAR包' }],
  };

  const visible = ref(false);
  const jarFileList = ref([]);

  function buildJarFileList() {
    if (form.jarPath) {
      jarFileList.value = [
        {
          uid: '-1',
          name: form.jarName || form.jarPath.split('/').pop() || form.jarPath,
          status: 'done',
          url: form.jarPath,
        },
      ];
    } else {
      jarFileList.value = [];
    }
  }

  watch(jarFileList, (val) => {
    if (val.length === 0) {
      form.jarPath = '';
    }
  });

  function showDrawer(rowData) {
    Object.assign(form, formDefault);
    if (rowData && !_.isEmpty(rowData)) {
      Object.assign(form, rowData);
    }
    buildJarFileList();
    visible.value = true;
    nextTick(() => {
      formRef.value.clearValidate();
    });
  }

  function onClose() {
    Object.assign(form, formDefault);
    jarFileList.value = [];
    visible.value = false;
  }

  const customUpload = async (options) => {
    SmartLoading.show();
    try {
      const formData = new FormData();
      formData.append('file', options.file);
      let res = await fileApi.uploadFile(formData, FILE_FOLDER_TYPE_ENUM.PROTOCOL.value);
      form.jarPath = res.data.fileUrl;
      form.jarName = options.file.name;
      options.onSuccess(res, options.file);
    } catch (e) {
      smartSentry.captureError(e);
      jarFileList.value = [];
      options.onError(e);
    } finally {
      SmartLoading.hide();
    }
  };

  function onSubmit() {
    formRef.value
      .validate()
      .then(async () => {
        SmartLoading.show();
        try {
          if (form.id) {
            await protocolApi.update(form);
          } else {
            await protocolApi.add(form);
          }
          message.success(`${form.id ? '修改' : '添加'}成功`);
          onClose();
          emit('reloadList');
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

  defineExpose({ showDrawer });
</script>
