/**
 * 物模型属性 读写权限常量
 *
 * @Author 廖涛
 * @Date 2026/06/14
 * @Copyright 1024创新实验室
 */
const MAP = {
  r: { label: '只读', color: 'blue', canRead: true, canWrite: false },
  rw: { label: '读写', color: 'green', canRead: true, canWrite: true },
  w: { label: '只写', color: 'orange', canRead: false, canWrite: true },
};

/** 根据权限标识获取完整描述对象 */
export function accessMode(mode) {
  return MAP[mode] || MAP.r;
}

/** label → accessMode 反向映射（供 enum 组件使用） */
export const ACCESS_MODE_ENUM = [
  { value: 'r', desc: '只读' },
  { value: 'rw', desc: '读写' },
  { value: 'w', desc: '只写' },
];

export default { accessMode, ACCESS_MODE_ENUM };
