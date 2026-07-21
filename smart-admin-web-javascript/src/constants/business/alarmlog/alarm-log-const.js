/*
 * 告警日志 常量
 *
 * @Author: 1024创新实验室
 * @Date: 2025-05-26
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
 */
export const ALARM_LEVEL_ENUM = {
  EMERGENCY: {
    value: 1,
    desc: '紧急',
  },
  IMPORTANT: {
    value: 2,
    desc: '重要',
  },
  MINOR: {
    value: 3,
    desc: '次要',
  },
  WARNING: {
    value: 4,
    desc: '提示',
  },
};

export const ALARM_STATUS_ENUM = {
  PENDING: {
    value: 1,
    desc: '未处理',
  },
  PROCESSING: {
    value: 2,
    desc: '处理中',
  },
  RESOLVED: {
    value: 3,
    desc: '已处理',
  },
  IGNORED: {
    value: 4,
    desc: '已忽略',
  },
};

export default {
  ALARM_LEVEL_ENUM,
  ALARM_STATUS_ENUM,
};
