export const UNIT_GROUPS = [
  {
    label: '常用单位',
    options: [
      { value: '%', label: '百分比(%)' },
      { value: 'count', label: '次' },
      { value: 'r/min', label: '转每分钟' },
    ],
  },
  {
    label: '长度单位',
    options: [
      { value: 'nm', label: '纳米(nm)' },
      { value: 'μm', label: '微米(μm)' },
      { value: 'mm', label: '毫米(mm)' },
      { value: 'cm', label: '厘米(cm)' },
      { value: 'm', label: '米(m)' },
      { value: 'km', label: '千米(km)' },
    ],
  },
  {
    label: '面积单位',
    options: [
      { value: 'mm²', label: '平方毫米(mm²)' },
      { value: 'cm²', label: '平方厘米(cm²)' },
      { value: 'm²', label: '平方米(m²)' },
      { value: 'km²', label: '平方千米(km²)' },
      { value: 'hm²', label: '公顷(hm²)' },
    ],
  },
  {
    label: '时间单位',
    options: [
      { value: 'd', label: '天(d)' },
      { value: 'h', label: '小时(h)' },
      { value: 'min', label: '分钟(min)' },
      { value: 's', label: '秒(s)' },
      { value: 'ms', label: '毫秒(ms)' },
      { value: 'μs', label: '微秒(μs)' },
      { value: 'ns', label: '纳秒(ns)' },
    ],
  },
  {
    label: '体积单位',
    options: [
      { value: 'mm³', label: '立方毫米(mm³)' },
      { value: 'cm³', label: '立方厘米(cm³)' },
      { value: 'm³', label: '立方米(m³)' },
      { value: 'km³', label: '立方千米(km³)' },
    ],
  },
  {
    label: '流量单位',
    options: [
      { value: 'm³/s', label: '立方米每秒(m³/s)' },
      { value: 'km³/s', label: '立方千米每秒(km³/s)' },
      { value: 'cm³/s', label: '立方厘米每秒(cm³/s)' },
      { value: 'l/s', label: '升每秒(l/s)' },
      { value: 'm³/h', label: '立方米每小时(m³/h)' },
      { value: 'km³/h', label: '立方千米每小时(km³/h)' },
      { value: 'cm³/h', label: '立方厘米每小时(cm³/h)' },
      { value: 'l/h', label: '升每小时(l/h)' },
    ],
  },
  {
    label: '容积单位',
    options: [
      { value: 'mL', label: '毫升(mL)' },
      { value: 'L', label: '升(L)' },
    ],
  },
  {
    label: '质量单位',
    options: [
      { value: 'mg', label: '毫克(mg)' },
      { value: 'g', label: '克(g)' },
      { value: 'kg', label: '千克(kg)' },
      { value: 't', label: '吨(t)' },
    ],
  },
  {
    label: '压力单位',
    options: [
      { value: 'Pa', label: '帕斯卡(Pa)' },
      { value: 'kPa', label: '千帕斯卡(kPa)' },
    ],
  },
  {
    label: '力单位',
    options: [
      { value: 'N', label: '牛顿(N)' },
      { value: 'N.m', label: '牛·米(N.m)' },
    ],
  },
  {
    label: '温度单位',
    options: [
      { value: 'K', label: '开尔文(K)' },
      { value: '℃', label: '摄氏度(℃)' },
      { value: '℉', label: '华氏度(℉)' },
    ],
  },
  {
    label: '能量单位',
    options: [
      { value: 'J', label: '焦耳(J)' },
      { value: 'cal', label: '卡(cal)' },
    ],
  },
  {
    label: '功率单位',
    options: [
      { value: 'W', label: '瓦特(W)' },
      { value: 'kW', label: '千瓦特(kW)' },
    ],
  },
  {
    label: '角度单位',
    options: [
      { value: 'rad', label: '弧度(rad)' },
      { value: '°', label: '度(°)' },
      { value: '′', label: '[角]分(′)' },
      { value: '″', label: '[角]秒(″)' },
    ],
  },
  {
    label: '频率单位',
    options: [
      { value: 'Hz', label: '赫兹(Hz)' },
      { value: 'MHz', label: '兆赫兹(MHz)' },
      { value: 'GHz', label: 'G赫兹(GHz)' },
    ],
  },
  {
    label: '速度单位',
    options: [
      { value: 'm/s', label: '米每秒(m/s)' },
      { value: 'km/h', label: '千米每小时(km/h)' },
      { value: 'kn', label: '节(kn)' },
    ],
  },
  {
    label: '电力单位',
    options: [
      { value: 'V', label: '伏特(V)' },
      { value: 'kV', label: '千伏(kV)' },
      { value: 'mV', label: '毫伏(mV)' },
      { value: 'μV', label: '微伏(μV)' },
      { value: 'A', label: '安培(A)' },
      { value: 'mA', label: '毫安(mA)' },
      { value: 'μA', label: '微安(μA)' },
      { value: 'nA', label: '纳安(nA)' },
      { value: 'Ω', label: '欧姆(Ω)' },
      { value: 'KΩ', label: '千欧(KΩ)' },
      { value: 'MΩ', label: '兆欧(MΩ)' },
      { value: 'eV', label: '电子伏(eV)' },
      { value: 'kW·h', label: '千瓦·时(kW·h)' },
    ],
  },
];
