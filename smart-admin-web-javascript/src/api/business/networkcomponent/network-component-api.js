/*
 * 网络组件
 *
 * @Author:   1024创新实验室
 * @Date:     2026-05-26
 * @Copyright 1024创新实验室 （ https://1024lab.net ），Since 2012
 */
import { postRequest, getRequest } from '/@/lib/axios';

export const networkComponentApi = {
  /**
   * 分页查询
   */
  queryPage: (param) => {
    return postRequest('/networkComponent/queryPage', param);
  },

  /**
   * 添加
   */
  add: (param) => {
    return postRequest('/networkComponent/add', param);
  },

  /**
   * 修改
   */
  update: (param) => {
    return postRequest('/networkComponent/update', param);
  },

  /**
   * 批量删除
   */
  batchDelete: (idList) => {
    return postRequest('/networkComponent/batchDelete', idList);
  },

  /**
   * 单个删除
   */
  delete: (id) => {
    return getRequest(`/networkComponent/delete/${id}`);
  },
};
