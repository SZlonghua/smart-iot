/**
 * 根据 DataType 校验单个值，返回错误信息字符串，合法返回 null
 * @param {string} path 当前字段路径，递归时自动拼接
 */
export function validateValue(val, valueType, path = '') {
  if (!valueType) return null;
  const t = valueType.type;
  const err = (msg) => (path ? `${path}: ${msg}` : msg);

  // 数字类型
  if (t === 'int' || t === 'long') {
    if (val === null || val === undefined || val === '') return err('请输入值');
    if (!Number.isInteger(Number(val))) return err('必须是整数');
    if (valueType.min !== undefined && val < valueType.min) return err(`不能小于${valueType.min}`);
    if (valueType.max !== undefined && val > valueType.max) return err(`不能大于${valueType.max}`);
  }
  if (t === 'float' || t === 'double') {
    if (val === null || val === undefined || val === '') return err('请输入值');
    if (isNaN(Number(val))) return err('必须是数字');
    if (valueType.min !== undefined && val < valueType.min) return err(`不能小于${valueType.min}`);
    if (valueType.max !== undefined && val > valueType.max) return err(`不能大于${valueType.max}`);
  }

  // 字符串
  if (t === 'string') {
    if (!val) return err('请输入值');
    if (valueType.maxLength && String(val).length > valueType.maxLength) return err(`不能超过${valueType.maxLength}个字符`);
  }

  // 布尔
  if (t === 'boolean') {
    if (val === null || val === undefined || val === '') return err('请选择值');
    const tv = valueType.trueValue || 'true';
    const fv = valueType.falseValue || 'false';
    if (String(val) !== tv && String(val) !== fv) return err(`必须是 ${tv} 或 ${fv}`);
  }

  // 日期
  if (t === 'date') return null;

  // 枚举
  if (t === 'enum') {
    if (val === null || val === undefined) return err('请选择值');
    const vals = (valueType.elements || []).map((e) => e.value);
    if (!vals.includes(String(val))) return err(`必须是: ${vals.join(', ')}`);
  }

  // 数组
  if (t === 'array') {
    const arr = Array.isArray(val)
      ? val
      : (() => {
          try {
            const p = JSON.parse(val);
            return Array.isArray(p) ? p : null;
          } catch {
            return null;
          }
        })();
    if (!arr) return err('必须是有效的JSON数组');
    if (arr.length === 0) return err('数组不能为空');
    if (!valueType.elementType) return null;
    const errs = [];
    arr.forEach((item, i) => {
      const sub = path ? `${path}[${i}]` : `[${i}]`;
      const e = validateValue(item, valueType.elementType, sub);
      if (e) errs.push(e);
    });
    return errs.length ? errs.join('; ') : null;
  }

  // 对象
  if (t === 'object') {
    const obj =
      typeof val === 'object' && !Array.isArray(val)
        ? val
        : (() => {
            try {
              const p = JSON.parse(val);
              return typeof p === 'object' && !Array.isArray(p) ? p : null;
            } catch {
              return null;
            }
          })();
    if (!obj) return err('必须是有效的JSON对象');
    if (!valueType.properties) return null;
    const errs = [];
    for (const prop of valueType.properties) {
      if (obj[prop.id] !== undefined) {
        const key = prop.id;
        const e = validateValue(obj[prop.id], prop.valueType, path ? `${path}.${key}` : key);
        if (e) errs.push(e);
      }
    }
    return errs.length ? errs.join('; ') : null;
  }

  return null;
}
