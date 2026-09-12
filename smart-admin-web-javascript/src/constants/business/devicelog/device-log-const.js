/*
 * 设备日志 常量 — 值即后端 TopicMessageCodec 枚举名（与存储 commandType 同源），
 * 查询多选直传无需映射；命令与回复成对相邻（回复为名称带 Reply 后缀的上行消息）
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
  DISCONNECT_REPLY: {
    value: 'disconnectReply',
    desc: '断开连接回复',
  },
  PROPERTIES_REPORT: {
    value: 'reportProperty',
    desc: '属性上报',
  },
  PROPERTIES_READ: {
    value: 'readProperty',
    desc: '读取属性',
  },
  PROPERTIES_READ_REPLY: {
    value: 'readPropertyReply',
    desc: '读取属性回复',
  },
  PROPERTIES_WRITE: {
    value: 'writeProperty',
    desc: '设置属性',
  },
  PROPERTIES_WRITE_REPLY: {
    value: 'writePropertyReply',
    desc: '设置属性回复',
  },
  EVENT: {
    value: 'event',
    desc: '事件',
  },
  COMMAND: {
    value: 'functionInvoke',
    desc: '命令',
  },
  COMMAND_REPLY: {
    value: 'functionInvokeReply',
    desc: '命令回复',
  },
};
export default {
  DEVICE_LOG_TYPE_ENUM,
};
