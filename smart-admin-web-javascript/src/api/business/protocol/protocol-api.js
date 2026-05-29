import { postRequest, getRequest } from '/@/lib/axios';

export const protocolApi = {
  /**
   * 分页查询
   */
  queryPage: (param) => postRequest('/protocol/queryPage', param),

  /**
   * 添加
   */
  add: (param) => postRequest('/protocol/add', param),

  /**
   * 修改
   */
  update: (param) => postRequest('/protocol/update', param),

  /**
   * 单个删除
   */
  delete: (id) => getRequest(`/protocol/delete/${id}`),

  /**
   * 批量删除
   */
  batchDelete: (idList) => postRequest('/protocol/batchDelete', idList),
};
