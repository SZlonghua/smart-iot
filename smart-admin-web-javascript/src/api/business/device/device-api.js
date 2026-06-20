import { postRequest, getRequest } from '/@/lib/axios';

export const deviceApi = {
  // ===== 设备管理 =====
  queryPage: (param) => postRequest('/device/queryPage', param),
  add: (param) => postRequest('/device/add', param),
  update: (param) => postRequest('/device/update', param),
  delete: (id) => getRequest(`/device/delete/${id}`),
  batchDelete: (idList) => postRequest('/device/batchDelete', idList),

  // ===== 设备详情 =====
  getDetail: (id) => getRequest(`/device/detail/${id}`),

  // ===== 属性操作 =====
  readProperties: (deviceId, properties) => postRequest(`/device/${deviceId}/properties/read`, properties),
  getProperties: (deviceId, properties) => postRequest(`/device/${deviceId}/properties`, properties),
  writeProperties: (deviceId, properties) => postRequest(`/device/setting/${deviceId}/property`, properties),

  // ===== 功能调用 =====
  invokeFunction: (deviceId, functionId, params) => postRequest(`/device/invoked/${deviceId}/function/${functionId}`, params),
};
