/*
 * 网络组件
 *
 * @Author:    廖涛
 * @Date:      2026-05-31
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

// 组件类型
export const COMPONENT_TYPE_ENUM = {
  TCP: { value: 'tcp', desc: 'TCP服务' },
  HTTP: { value: 'http', desc: 'HTTP服务' },
  MQTT_SERVER: { value: 'mqtt-server', desc: 'MQTT服务' },
  MQTT_CLIENT: { value: 'mqtt-client', desc: 'MQTT客户端' },
};

// 各组件类型的默认配置模板
export const COMPONENT_DEFAULT_CONFIG = {
  'tcp': {
    localAddress: '0.0.0.0',
    localPort: null,
    publicAddress: '',
    publicPort: null,
    tlsEnabled: false,
  },
  'http': {
    localAddress: '0.0.0.0',
    localPort: null,
    publicAddress: '',
    publicPort: null,
    tlsEnabled: false,
  },
  'mqtt-server': {
    localAddress: '0.0.0.0',
    localPort: null,
    publicAddress: '',
    publicPort: null,
    maxMessageSize: 8192,
    tlsEnabled: false,
  },
  'mqtt-client': {
    remoteAddress: '',
    remotePort: null,
    clientId: '',
    username: '',
    password: '',
    maxMessageSize: 8192,
    tlsEnabled: false,
  },
};

export default {
  COMPONENT_TYPE_ENUM,
  COMPONENT_DEFAULT_CONFIG,
};
