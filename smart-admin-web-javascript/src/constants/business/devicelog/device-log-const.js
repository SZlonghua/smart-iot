/*
 * 设备日志 常量
 *
 * @Author: 1024创新实验室
 * @Date: 2025-05-26
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
 */
export const DEVICE_LOG_TYPE_ENUM = {
  ONLINE: {
    value: 'online',
    desc: '上线',
  },
  OFFLINE: {
    value: 'offline',
    desc: '离线',
  },
  REGISTER: {
    value: 'register',
    desc: '注册',
  },
  UNREGISTER: {
    value: 'unregister',
    desc: '注销',
  },
  DISCONNECT: {
    value: 'disconnect',
    desc: '断开连接',
  },
  PROPERTIES_REPORT: {
    value: 'properties_report',
    desc: '属性上报',
  },
  PROPERTIES_READ: {
    value: 'properties_read',
    desc: '读取属性',
  },
  PROPERTIES_WRITE: {
    value: 'properties_write',
    desc: '设置属性',
  },
  EVENT: {
    value: 'event',
    desc: '事件',
  },
  COMMAND: {
    value: 'command',
    desc: '命令',
  },
};
export default {
  DEVICE_LOG_TYPE_ENUM,
};
