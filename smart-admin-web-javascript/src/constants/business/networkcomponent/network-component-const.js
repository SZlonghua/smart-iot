/*
 * 网络组件
 *
 * @Author:    1024创新实验室
 * @Date:      2026-05-26
 * @Copyright  1024创新实验室 （ https://1024lab.net ），Since 2012
 */

export const COMPONENT_TYPE_ENUM = {
  MQTT_BROKER: {
    value: 'mqtt_broker',
    desc: 'MQTT Broker',
  },
  HTTP_SERVER: {
    value: 'http_server',
    desc: 'HTTP Server',
  },
  COAP_SERVER: {
    value: 'coap_server',
    desc: 'CoAP Server',
  },
  TCP_SERVER: {
    value: 'tcp_server',
    desc: 'TCP Server',
  },
};

export default {
  COMPONENT_TYPE_ENUM,
};
