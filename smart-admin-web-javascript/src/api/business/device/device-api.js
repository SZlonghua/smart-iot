import { postRequest, getRequest } from '/@/lib/axios';

export const deviceApi = {
  /**
   * 分页查询
   */
  queryPage: (param) => postRequest('/device/queryPage', param),

  /**
   * 添加
   */
  add: (param) => postRequest('/device/add', param),

  /**
   * 修改
   */
  update: (param) => postRequest('/device/update', param),

  /**
   * 单个删除
   */
  delete: (id) => getRequest(`/device/delete/${id}`),

  /**
   * 批量删除
   */
  batchDelete: (idList) => postRequest('/device/batchDelete', idList),
};
