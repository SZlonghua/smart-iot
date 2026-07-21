import { postRequest, getRequest } from '/@/lib/axios';

export const productCategoryApi = {
  /**
   * 分页查询
   */
  queryPage: (param) => postRequest('/productCategory/queryPage', param),

  /**
   * 查询分类树
   */
  queryTree: () => getRequest('/productCategory/queryTree'),

  /**
   * 查询子分类
   */
  queryChildren: (parentId) => getRequest(`/productCategory/children/${parentId}`),

  /**
   * 添加
   */
  add: (param) => postRequest('/productCategory/add', param),

  /**
   * 修改
   */
  update: (param) => postRequest('/productCategory/update', param),

  /**
   * 单个删除
   */
  delete: (id) => getRequest(`/productCategory/delete/${id}`),

  /**
   * 批量删除
   */
  batchDelete: (idList) => postRequest('/productCategory/batchDelete', idList),
};
