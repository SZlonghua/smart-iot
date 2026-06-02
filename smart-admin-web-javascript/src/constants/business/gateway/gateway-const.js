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

export default {
  ACCESS_TYPE_ENUM,
  ACCESS_TYPE_COMPONENT_MAP,
  COMPONENT_TYPE_TRANSPORT_MAP,
};
