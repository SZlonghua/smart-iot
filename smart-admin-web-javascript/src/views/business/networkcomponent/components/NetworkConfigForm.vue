<template>
  <div class="network-config-form">
    <!-- TCP服务 / HTTP服务 / MQTT服务 共用：本地配置 -->
    <template v-if="isServerType">
      <a-form-item label="本地地址" :name="[fieldName, 'localAddress']" :rules="[{ required: true, message: '本地地址不能为空' }]">
        <a-input :value="localConfig.localAddress" placeholder="固定为 0.0.0.0" disabled />
      </a-form-item>
      <a-form-item label="本地端口" :name="[fieldName, 'localPort']" :rules="[{ required: true, message: '本地端口不能为空' }]">
        <a-input-number :value="localConfig.localPort" placeholder="请输入本地端口" style="width: 100%" :min="1" :max="65535" :disabled="disabled" @update:value="v => updateField('localPort', v)" />
      </a-form-item>
      <a-form-item label="公网地址" :name="[fieldName, 'publicAddress']" :rules="[{ required: true, message: '公网地址不能为空' }]">
        <a-input :value="localConfig.publicAddress" placeholder="请输入公网地址" :disabled="disabled" @update:value="v => updateField('publicAddress', v)" />
      </a-form-item>
      <a-form-item label="公网端口" :name="[fieldName, 'publicPort']" :rules="[{ required: true, message: '公网端口不能为空' }]">
        <a-input-number :value="localConfig.publicPort" placeholder="请输入公网端口" style="width: 100%" :min="1" :max="65535" :disabled="disabled" @update:value="v => updateField('publicPort', v)" />
      </a-form-item>
    </template>

    <!-- MQTT客户端 专用：远程配置 -->
    <template v-if="type === 'mqtt-client'">
      <a-form-item label="远程地址" :name="[fieldName, 'remoteAddress']" :rules="[{ required: true, message: '远程地址不能为空' }]">
        <a-input :value="localConfig.remoteAddress" placeholder="请输入远程地址" :disabled="disabled" @update:value="v => updateField('remoteAddress', v)" />
      </a-form-item>
      <a-form-item label="远程端口" :name="[fieldName, 'remotePort']" :rules="[{ required: true, message: '远程端口不能为空' }]">
        <a-input-number :value="localConfig.remotePort" placeholder="请输入远程端口" style="width: 100%" :min="1" :max="65535" :disabled="disabled" @update:value="v => updateField('remotePort', v)" />
      </a-form-item>
      <a-form-item label="Client ID" :name="[fieldName, 'clientId']" :rules="[{ required: true, message: 'Client ID不能为空' }]">
        <a-input :value="localConfig.clientId" placeholder="请输入Client ID" :disabled="disabled" @update:value="v => updateField('clientId', v)" />
      </a-form-item>
      <a-form-item label="用户名" :name="[fieldName, 'username']" :rules="[{ required: true, message: '用户名不能为空' }]">
        <a-input :value="localConfig.username" placeholder="请输入用户名" :disabled="disabled" @update:value="v => updateField('username', v)" />
      </a-form-item>
      <a-form-item label="密码" :name="[fieldName, 'password']" :rules="[{ required: true, message: '密码不能为空' }]">
        <a-input-password :value="localConfig.password" placeholder="请输入密码" :disabled="disabled" @update:value="v => updateField('password', v)" />
      </a-form-item>
    </template>

    <!-- MQTT服务 / MQTT客户端 共用：最大消息长度 -->
    <template v-if="type === 'mqtt-server' || type === 'mqtt-client'">
      <a-form-item label="最大消息长度" :name="[fieldName, 'maxMessageSize']" :rules="[{ required: true, message: '最大消息长度不能为空' }]">
        <a-input-number :value="localConfig.maxMessageSize" placeholder="请输入最大消息长度" style="width: 100%" :min="1" :disabled="disabled" @update:value="v => updateField('maxMessageSize', v)" />
      </a-form-item>
    </template>

    <!-- 所有类型共用：开启TLS -->
    <a-form-item label="开启TLS" :name="[fieldName, 'tlsEnabled']" :rules="[{ required: true, message: '请选择是否开启TLS' }]">
      <a-radio-group :value="localConfig.tlsEnabled" :disabled="disabled" @update:value="v => updateField('tlsEnabled', v)">
        <a-radio :value="true">是</a-radio>
        <a-radio :value="false">否</a-radio>
      </a-radio-group>
    </a-form-item>
  </div>
</template>

<script setup>
import { reactive, computed, watch } from 'vue';

const props = defineProps({
  type: { type: String, default: '' },
  modelValue: { type: Object, default: () => ({}) },
  fieldName: { type: String, default: 'config' },
  disabled: { type: Boolean, default: false },
});

const emit = defineEmits(['update:modelValue']);

const isServerType = computed(() => ['tcp', 'http', 'mqtt-server'].includes(props.type));

const localConfig = reactive({ ...props.modelValue });

watch(
  () => props.modelValue,
  (val) => {
    if (val && typeof val === 'object') {
      Object.keys(localConfig).forEach((k) => delete localConfig[k]);
      Object.assign(localConfig, val);
    }
  },
  { deep: true }
);

function updateField(key, value) {
  localConfig[key] = value;
  emit('update:modelValue', { ...localConfig });
}
</script>

<style lang="less" scoped>
.network-config-form {
  padding: 4px 0;
}
</style>
