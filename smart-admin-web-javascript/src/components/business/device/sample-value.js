/**
 * 根据 DataType 递归生成示例值
 */
export function sampleValue(valueType) {
  if (!valueType) return '';
  const t = valueType.type;
  if (t === 'int' || t === 'long') return 0;
  if (t === 'float' || t === 'double') return 0.0;
  if (t === 'boolean') return valueType.trueValue || 'true';
  if (t === 'string') return '示例文本';
  if (t === 'date') return '2026-01-01 00:00:00';
  if (t === 'enum') return valueType.elements?.[0]?.value || '';
  if (t === 'array') return valueType.elementType ? [sampleValue(valueType.elementType)] : [];
  if (t === 'object') {
    const o = {};
    (valueType.properties || []).forEach((p) => {
      o[p.id] = sampleValue(p.valueType);
    });
    return o;
  }
  return '';
}
