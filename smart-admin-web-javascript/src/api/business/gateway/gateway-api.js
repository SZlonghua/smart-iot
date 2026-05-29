/*
 * 设备网关
 *
 * @Author:   1024创新实验室
 * @Date:     2026-05-26
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const gatewayApi = {
  /**
   * 分页查询
   */
  queryPage: (param) => {
    return postRequest('/gateway/queryPage', param);
  },

  /**
   * 添加
   */
  add: (param) => {
    return postRequest('/gateway/add', param);
  },

  /**
   * 修改
   */
  update: (param) => {
    return postRequest('/gateway/update', param);
  },

  /**
   * 批量删除
   */
  batchDelete: (idList) => {
    return postRequest('/gateway/batchDelete', idList);
  },

  /**
   * 单个删除
   */
  delete: (id) => {
    return getRequest(`/gateway/delete/${id}`);
  },
};
