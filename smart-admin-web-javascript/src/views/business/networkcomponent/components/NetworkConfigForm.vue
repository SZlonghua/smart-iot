<template>
  <div class="network-config-form">
    <!-- TCP服务 -->
    <template v-if="type === 'tcp'">
      <a-form-item label="本地地址" :name="['config', 'localAddress']" :rules="[{ required: true, message: '本地地址不能为空' }]">
        <a-input :value="config.localAddress" placeholder="固定为 0.0.0.0" disabled />
      </a-form-item>
      <a-form-item label="本地端口" :name="['config', 'localPort']" :rules="[{ required: true, message: '本地端口不能为空' }]">
        <a-input-number v-model:value="config.localPort" placeholder="请输入本地端口" style="width: 100%" :min="1" :max="65535" />
      </a-form-item>
      <a-form-item label="公网地址" :name="['config', 'publicAddress']" :rules="[{ required: true, message: '公网地址不能为空' }]">
        <a-input v-model:value="config.publicAddress" placeholder="请输入公网地址" />
      </a-form-item>
      <a-form-item label="公网端口" :name="['config', 'publicPort']" :rules="[{ required: true, message: '公网端口不能为空' }]">
        <a-input-number v-model:value="config.publicPort" placeholder="请输入公网端口" style="width: 100%" :min="1" :max="65535" />
      </a-form-item>
      <a-form-item label="开启TLS" :name="['config', 'tlsEnabled']" :rules="[{ required: true, message: '请选择是否开启TLS' }]">
        <a-radio-group v-model:value="config.tlsEnabled">
          <a-radio :value="true">是</a-radio>
          <a-radio :value="false">否</a-radio>
        </a-radio-group>
      </a-form-item>
    </template>

    <!-- HTTP服务 -->
    <template v-if="type === 'http'">
      <a-form-item label="本地地址" :name="['config', 'localAddress']" :rules="[{ required: true, message: '本地地址不能为空' }]">
        <a-input :value="config.localAddress" placeholder="固定为 0.0.0.0" disabled />
      </a-form-item>
      <a-form-item label="本地端口" :name="['config', 'localPort']" :rules="[{ required: true, message: '本地端口不能为空' }]">
        <a-input-number v-model:value="config.localPort" placeholder="请输入本地端口" style="width: 100%" :min="1" :max="65535" />
      </a-form-item>
      <a-form-item label="公网地址" :name="['config', 'publicAddress']" :rules="[{ required: true, message: '公网地址不能为空' }]">
        <a-input v-model:value="config.publicAddress" placeholder="请输入公网地址" />
      </a-form-item>
      <a-form-item label="公网端口" :name="['config', 'publicPort']" :rules="[{ required: true, message: '公网端口不能为空' }]">
        <a-input-number v-model:value="config.publicPort" placeholder="请输入公网端口" style="width: 100%" :min="1" :max="65535" />
      </a-form-item>
      <a-form-item label="开启TLS" :name="['config', 'tlsEnabled']" :rules="[{ required: true, message: '请选择是否开启TLS' }]">
        <a-radio-group v-model:value="config.tlsEnabled">
          <a-radio :value="true">是</a-radio>
          <a-radio :value="false">否</a-radio>
        </a-radio-group>
      </a-form-item>
    </template>

    <!-- MQTT服务 -->
    <template v-if="type === 'mqtt-server'">
      <a-form-item label="本地地址" :name="['config', 'localAddress']" :rules="[{ required: true, message: '本地地址不能为空' }]">
        <a-input :value="config.localAddress" placeholder="固定为 0.0.0.0" disabled />
      </a-form-item>
      <a-form-item label="本地端口" :name="['config', 'localPort']" :rules="[{ required: true, message: '本地端口不能为空' }]">
        <a-input-number v-model:value="config.localPort" placeholder="请输入本地端口" style="width: 100%" :min="1" :max="65535" />
      </a-form-item>
      <a-form-item label="公网地址" :name="['config', 'publicAddress']" :rules="[{ required: true, message: '公网地址不能为空' }]">
        <a-input v-model:value="config.publicAddress" placeholder="请输入公网地址" />
      </a-form-item>
      <a-form-item label="公网端口" :name="['config', 'publicPort']" :rules="[{ required: true, message: '公网端口不能为空' }]">
        <a-input-number v-model:value="config.publicPort" placeholder="请输入公网端口" style="width: 100%" :min="1" :max="65535" />
      </a-form-item>
      <a-form-item label="最大消息长度" :name="['config', 'maxMessageSize']" :rules="[{ required: true, message: '最大消息长度不能为空' }]">
        <a-input-number v-model:value="config.maxMessageSize" placeholder="请输入最大消息长度" style="width: 100%" :min="1" />
      </a-form-item>
      <a-form-item label="开启TLS" :name="['config', 'tlsEnabled']" :rules="[{ required: true, message: '请选择是否开启TLS' }]">
        <a-radio-group v-model:value="config.tlsEnabled">
          <a-radio :value="true">是</a-radio>
          <a-radio :value="false">否</a-radio>
        </a-radio-group>
      </a-form-item>
    </template>

    <!-- MQTT客户端 -->
    <template v-if="type === 'mqtt-client'">
      <a-form-item label="远程地址" :name="['config', 'remoteAddress']" :rules="[{ required: true, message: '远程地址不能为空' }]">
        <a-input v-model:value="config.remoteAddress" placeholder="请输入远程地址" />
      </a-form-item>
      <a-form-item label="远程端口" :name="['config', 'remotePort']" :rules="[{ required: true, message: '远程端口不能为空' }]">
        <a-input-number v-model:value="config.remotePort" placeholder="请输入远程端口" style="width: 100%" :min="1" :max="65535" />
      </a-form-item>
      <a-form-item label="Client ID" :name="['config', 'clientId']" :rules="[{ required: true, message: 'Client ID不能为空' }]">
        <a-input v-model:value="config.clientId" placeholder="请输入Client ID" />
      </a-form-item>
      <a-form-item label="用户名" :name="['config', 'username']" :rules="[{ required: true, message: '用户名不能为空' }]">
        <a-input v-model:value="config.username" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item label="密码" :name="['config', 'password']" :rules="[{ required: true, message: '密码不能为空' }]">
        <a-input-password v-model:value="config.password" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item label="最大消息长度" :name="['config', 'maxMessageSize']" :rules="[{ required: true, message: '最大消息长度不能为空' }]">
        <a-input-number v-model:value="config.maxMessageSize" placeholder="请输入最大消息长度" style="width: 100%" :min="1" />
      </a-form-item>
      <a-form-item label="开启TLS" :name="['config', 'tlsEnabled']" :rules="[{ required: true, message: '请选择是否开启TLS' }]">
        <a-radio-group v-model:value="config.tlsEnabled">
          <a-radio :value="true">是</a-radio>
          <a-radio :value="false">否</a-radio>
        </a-radio-group>
      </a-form-item>
    </template>
  </div>
</template>

<script setup>
  import { defineModel } from 'vue';

  defineProps({
    type: { type: String, default: '' },
  });

  const config = defineModel('config', { type: Object, required: true });
</script>

<style lang="less" scoped>
  .network-config-form {
    padding: 4px 0;
  }
</style>
