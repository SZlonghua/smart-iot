/**
 * 数据类型 → 编辑器组件映射 + 选项
 * 新增类型只需在此注册 + 创建 types/TypeXxx.vue
 */
import TypeNumeric from './types/TypeNumeric.vue';
import TypeBoolean from './types/TypeBoolean.vue';
import TypeString from './types/TypeString.vue';
import TypeEnum from './types/TypeEnum.vue';
import TypeDate from './types/TypeDate.vue';
import TypeArray from './types/TypeArray.vue';
import TypeObject from './types/TypeObject.vue';

export const typeEditorMap = {
  int: TypeNumeric,
  double: TypeNumeric,
  float: TypeNumeric,
  long: TypeNumeric,
  boolean: TypeBoolean,
  string: TypeString,
  enum: TypeEnum,
  date: TypeDate,
  array: TypeArray,
  object: TypeObject,
};

export const TYPE_OPTIONS = [
  { value: 'int', label: 'int (整数)' },
  { value: 'double', label: 'double (双精度)' },
  { value: 'float', label: 'float (单精度)' },
  { value: 'long', label: 'long (长整数)' },
  { value: 'boolean', label: 'boolean (布尔)' },
  { value: 'string', label: 'string (字符串)' },
  { value: 'enum', label: 'enum (枚举)' },
  { value: 'date', label: 'date (日期)' },
  { value: 'array', label: 'array (数组)' },
  { value: 'object', label: 'object (对象)' },
];
