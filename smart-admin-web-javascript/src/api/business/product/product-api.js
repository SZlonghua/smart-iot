import { postRequest, getRequest } from '/@/lib/axios';

export const productApi = {
  /**
   * 分页查询
   */
  queryPage: (param) => postRequest('/product/queryPage', param),

  /**
   * 添加
   */
  add: (param) => postRequest('/product/add', param),

  /**
   * 修改
   */
  update: (param) => postRequest('/product/update', param),

  /**
   * 单个删除
   */
  delete: (id) => getRequest(`/product/delete/${id}`),

  /**
   * 批量删除
   */
  batchDelete: (idList) => postRequest('/product/batchDelete', idList),
};
