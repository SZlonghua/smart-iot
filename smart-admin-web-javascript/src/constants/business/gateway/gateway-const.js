/*
 * 设备网关
 *
 * @Author:    廖涛
 * @Date:      2026-06-01
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

// 接入类型
export const ACCESS_TYPE_ENUM = {
  MQTT_DIRECT: { value: 'mqtt_direct', desc: 'MQTT直接连接' },
  GATEWAY_CHILD: { value: 'gateway_child', desc: '网关子设备接入' },
  HTTP_PUSH: { value: 'http_push', desc: 'HTTP推送接入' },
  MQTT_BROKER: { value: 'mqtt_broker', desc: 'MQTT Broker接入' },
  TCP_TRANSPARENT: { value: 'tcp_transparent', desc: 'TCP透传接入' },
};

// 接入类型 → 需要的网络组件类型（null 表示不需要）
export const ACCESS_TYPE_COMPONENT_MAP = {
  'mqtt_direct': 'mqtt-server',
  'gateway_child': null,
  'http_push': 'http',
  'mqtt_broker': 'mqtt-client',
  'tcp_transparent': 'tcp',
};

// 网络组件类型 → transport 值
export const COMPONENT_TYPE_TRANSPORT_MAP = {
  'mqtt-server': 'MQTT',
  'http': 'HTTP',
  'mqtt-client': 'MQTT',
  'tcp': 'TCP',
};

// 网络组件 配置项 label 映射
export const NETWORK_COMPONENT_CONFIG_LABEL_MAP = {
  localAddress: '本地地址',
  localPort: '本地端口',
  publicAddress: '公网地址',
  publicPort: '公网端口',
  remoteAddress: '远程地址',
  remotePort: '远程端口',
  clientId: 'Client ID',
  username: '用户名',
  password: '密码',
  maxMessageSize: '最大消息长度',
  tlsEnabled: '开启TLS',
};

export default {
  ACCESS_TYPE_ENUM,
  ACCESS_TYPE_COMPONENT_MAP,
  COMPONENT_TYPE_TRANSPORT_MAP,
  NETWORK_COMPONENT_CONFIG_LABEL_MAP,
};
