import { postRequest, getRequest } from '/@/lib/axios';

export const productApi = {
  /**
   * 分页查询
   */
  queryPage: (param) => postRequest('/product/queryPage', param),

  /**
   * 查询详情
   */
  getById: (id) => getRequest(`/product/getById/${id}`),

  /**
   * 添加
   */
  add: (param) => postRequest('/product/add', param),

  /**
   * 保存物模型
   */
  saveModel: (param) => postRequest('/product/saveModel', param),

  /**
   * 切换启用/禁用
   */
  toggleStatus: (param) => postRequest('/product/toggleStatus', param),

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
