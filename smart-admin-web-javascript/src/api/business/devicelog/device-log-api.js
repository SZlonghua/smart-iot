/*
 * 设备日志 API
 *
 * @Author: 1024创新实验室
 * @Date: 2025-05-26
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import { postRequest } from '/@/lib/axios';

export const deviceLogApi = {
  /**
   * 分页查询
   */
  queryPage: (param) => {
    return postRequest('/deviceLog/queryPage', param);
  },
};
