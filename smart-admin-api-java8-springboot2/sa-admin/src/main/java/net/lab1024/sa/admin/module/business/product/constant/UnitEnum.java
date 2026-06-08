package net.lab1024.sa.admin.module.business.product.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 物模型-单位枚举
 *
 * @Author 廖涛
 * @Date 2026/06/04
 * @Copyright 1024创新实验室
 */
@AllArgsConstructor
@Getter
public enum UnitEnum implements BaseEnum {

    // 常用单位
    PERCENT("percent", "百分比", "%", "常用单位", "百分比(%)"),
    COUNT("count", "次", "count", "常用单位", "次"),
    TURN_PER_SECONDS("turnPerSeconds", "转每分钟", "r/min", "常用单位", "转每分钟"),

    // 长度单位
    NANOMETER("nanometer", "纳米", "nm", "长度单位", "长度单位:纳米(nm)"),
    MICRON("micron", "微米", "μm", "长度单位", "长度单位:微米(μm)"),
    MILLIMETER("millimeter", "毫米", "mm", "长度单位", "长度单位:毫米(mm)"),
    CENTIMETER("centimeter", "厘米", "cm", "长度单位", "长度单位:厘米(cm)"),
    METER("meter", "米", "m", "长度单位", "长度单位:米(m)"),
    KILOMETER("kilometer", "千米", "km", "长度单位", "长度单位:千米(km)"),

    // 面积单位
    SQUARE_MILLIMETER("squareMillimeter", "平方毫米", "mm²", "面积单位", "面积单位:平方毫米(mm²)"),
    SQUARE_CENTIMETER("squareCentimeter", "平方厘米", "cm²", "面积单位", "面积单位:平方厘米(cm²)"),
    SQUARE_METER("squareMeter", "平方米", "m²", "面积单位", "面积单位:平方米(m²)"),
    SQUARE_KILOMETER("squareKilometer", "平方千米", "km²", "面积单位", "面积单位:平方千米(km²)"),
    HECTARE("hectare", "公顷", "hm²", "面积单位", "面积单位:公顷(hm²)"),

    // 时间单位
    DAYS("days", "天", "d", "时间单位", "时间单位:天(d)"),
    HOUR("hour", "小时", "h", "时间单位", "时间单位:小时(h)"),
    MINUTES("minutes", "分钟", "min", "时间单位", "时间单位:分钟(m)"),
    SECONDS("seconds", "秒", "s", "时间单位", "时间单位:秒(s)"),
    MILLISECONDS("milliseconds", "毫秒", "ms", "时间单位", "时间单位:毫秒(ms)"),
    MICROSECONDS("microseconds", "微秒", "μs", "时间单位", "时间单位:微秒(μs)"),
    NANOSECONDS("nanoseconds", "纳秒", "ns", "时间单位", "时间单位:纳秒(ns)"),

    // 体积单位
    CUBIC_MILLIMETER("cubicMillimeter", "立方毫米", "mm³", "体积单位", "体积单位:立方毫米(mm³)"),
    CUBIC_CENTIMETER("cubicCentimeter", "立方厘米", "cm³", "体积单位", "体积单位:立方厘米(cm³)"),
    CUBIC_METER("cubicMeter", "立方米", "m³", "体积单位", "体积单位:立方米(m³)"),
    CUBIC_KILOMETER("cubicKilometer", "立方千米", "km³", "体积单位", "体积单位:立方千米(km³)"),

    // 流量单位
    CUBIC_METER_PER_SEC("cubicMeterPerSec", "立方米每秒", "m³/s", "流量单位", "流量单位:立方米每秒(m³/s)"),
    CUBIC_KILOMETER_PER_SEC("cubicKilometerPerSec", "立方千米每秒", "km³/s", "流量单位", "流量单位:立方千米每秒(km³/s)"),
    CUBIC_CENTIMETER_PER_SEC("cubicCentimeterPerSec", "立方厘米每秒", "cm³/s", "流量单位", "流量单位:立方厘米每秒(cm³/s)"),
    LITRE_PER_SEC("litrePerSec", "升每秒", "l/s", "流量单位", "流量单位:升每秒(l/s)"),
    CUBIC_METER_PER_HOUR("cubicMeterPerHour", "立方米每小时", "m³/h", "流量单位", "流量单位:立方米每小时(m³/h)"),
    CUBIC_KILOMETER_PER_HOUR("cubicKilometerPerHour", "立方千米每小时", "km³/h", "流量单位", "流量单位:立方千米每小时(km³/h)"),
    CUBIC_CENTIMETER_PER_HOUR("cubicCentimeterPerHour", "立方厘米每小时", "cm³/h", "流量单位", "流量单位:立方厘米每小时(cm³/h)"),
    LITRE_PER_HOUR("litrePerHour", "升每小时", "l/h", "流量单位", "流量单位:升每小时(l/h)"),

    // 容积单位
    MILLILITER("milliliter", "毫升", "mL", "容积单位", "容积单位:毫升(mL)"),
    LITRE("litre", "升", "L", "容积单位", "容积单位:升(L)"),

    // 质量单位
    MILLIGRAM("milligram", "毫克", "mg", "质量单位", "重量单位:毫克(mg)"),
    GRAMME("gramme", "克", "g", "质量单位", "重量单位:克(g)"),
    KILOGRAM("kilogram", "千克", "kg", "质量单位", "重量单位:千克(kg)"),
    TON("ton", "吨", "t", "质量单位", "重量单位:吨(t)"),

    // 压力单位
    PASCAL("pascal", "帕斯卡", "Pa", "压力单位", "压力单位:帕斯卡(Pa)"),
    KILO_PASCAL("kiloPascal", "千帕斯卡", "kPa", "压力单位", "压力单位:千帕斯卡(kPa)"),

    // 力单位
    NEWTON("newton", "牛顿", "N", "力单位", "力单位:牛顿(N)"),
    NEWTON_METER("newtonMeter", "牛·米", "N.m", "力单位", "力单位:牛·米(N.m)"),

    // 温度单位
    KELVIN("kelvin", "开尔文", "K", "温度单位", "温度单位:开尔文(K)"),
    CELSIUS_DEGREES("celsiusDegrees", "摄氏度", "℃", "温度单位", "温度单位:摄氏度(℃)"),
    FAHRENHEIT("fahrenheit", "华氏度", "℉", "温度单位", "温度单位:华氏度(℉)"),

    // 能量单位
    JOULE("joule", "焦耳", "J", "能量单位", "能单位:焦耳(J)"),
    CAL("cal", "卡", "cal", "能量单位", "能单位:卡(cal)"),

    // 功率单位
    WATT("watt", "瓦特", "W", "功率单位", "功率单位:瓦特(W)"),
    KILOWATT("kilowatt", "千瓦特", "kW", "功率单位", "功率单位:千瓦特(kW)"),

    // 角度单位
    RADIAN("radian", "弧度", "rad", "角度单位", "角度单位:弧度(rad)"),
    DEGREES("degrees", "度", "°", "角度单位", "角度单位:度(°)"),
    FEN("fen", "[角]分", "′", "角度单位", "角度单位:分(′)"),
    ANGLE_SECONDS("angleSeconds", "[角]秒", "″", "角度单位", "角度单位:度(″)"),

    // 频率单位
    HERTZ("hertz", "赫兹", "Hz", "频率单位", "频率单位:赫兹(Hz)"),
    MEGAHERTZ("megahertz", "兆赫兹", "MHz", "频率单位", "频率单位:兆赫兹(MHz)"),
    GHERTZ("ghertz", "G赫兹", "GHz", "频率单位", "频率单位:G赫兹(GHz)"),

    // 速度单位
    M_PER_SEC("mPerSec", "米每秒", "m/s", "速度单位", "速度单位:米每秒(m/s)"),
    KM_PER_HR("kmPerHr", "千米每小时", "km/h", "速度单位", "速度单位:千米每小时(km/h)"),
    KNOTS("knots", "节", "kn", "速度单位", "速度单位:节(kn)"),

    // 电力单位
    VOLT("volt", "伏特", "V", "电力单位", "电压:伏特(V)"),
    KILO_VOLT("kiloVolt", "千伏", "kV", "电力单位", "电压:千伏(kV)"),
    MILLI_VOLT("milliVolt", "毫伏", "mV", "电力单位", "电压:毫伏(mV)"),
    MICRO_VOLT("microVolt", "微伏", "μV", "电力单位", "电压:微伏(μV)"),
    AMPERE("ampere", "安培", "A", "电力单位", "电流:安培(A)"),
    MILLI_AMPERE("milliAmpere", "毫安", "mA", "电力单位", "电流:毫安(mA)"),
    MICRO_AMPERE("microAmpere", "微安", "μA", "电力单位", "电流:微安(μA)"),
    NANO_AMPERE("nanoAmpere", "纳安", "nA", "电力单位", "电流:纳安(nA)"),
    OHM("ohm", "欧姆", "Ω", "电力单位", "电阻:欧姆(Ω)"),
    KILO_OHM("kiloOhm", "千欧", "KΩ", "电力单位", "电阻:千欧(KΩ)"),
    MILLION_OHM("millionOhm", "兆欧", "MΩ", "电力单位", "电阻:兆欧(MΩ)"),
    ELECTRON_VOLTS("electronVolts", "电子伏", "eV", "电力单位", "能单位:电子伏(eV)"),
    K_WATTS_HOUR("kWattsHour", "千瓦·时", "kW·h", "电力单位", "能单位:千瓦·时(kW·h)");

    private final String value;
    private final String name;
    private final String symbol;
    private final String type;
    private final String desc;

    /**
     * 根据symbol获取枚举
     */
    public static UnitEnum getBySymbol(String symbol) {
        if (symbol == null) return null;
        for (UnitEnum e : values()) {
            if (e.symbol.equals(symbol)) return e;
        }
        return null;
    }
}
