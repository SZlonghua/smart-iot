package net.lab1024.sa.base.storage.timeseries;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * 时序存储家族命名方案（布局常量单一来源）— 表名/tag/列名约定与值落列分派集中于此，供行/列布局策略
 * 与行协议构建共享，避免字面量散落各处漂移。将来接入新时序库（influxdb 等）沿用同一命名，
 * 与 TDengine 策略数据形态一致，产品切换存储策略不破坏历史查询语义。
 * <p>
 * 布局两轴：①行形态 row — 一条消息一行（属性摊平为动态列 / 事件与日志固定列），tags={device_id,...}；
 * ②单值列形态 column（仅属性表）— 每个属性值一行，tags={device_id, property}，
 * fields={message_id,create_time,device_name} ∪ 单值类型列（值落哪个类型列由 Java 值类型决定，见
 * {@link #valueColumnName}，与行协议类型分派对齐）。
 * <p>
 * 命名一套到底：tag/列名/单值类型列均下划线小写，写侧名即读侧行键（读侧按本类常量取值，不在别处重复声明）；
 * 仅动态属性列名（用户自定义）经 {@link #rowKey} 归一化。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
public final class TimeseriesLayout {

    private TimeseriesLayout() {
    }

    // ===== 逻辑表名（表 = device_xxx_{productId}，按产品隔离，表数 = 产品数）=====
    public static final String TABLE_PREFIX_PROPERTY = "device_properties_";
    public static final String TABLE_PREFIX_EVENT = "device_events_";
    public static final String TABLE_PREFIX_MESSAGE_LOG = "device_message_logs_";

    // ===== tag 键（查询维度；TDengine schemaless 按位置解析为 NCHAR）=====
    /** 设备 id（全部三表） */
    public static final String TAG_DEVICE_ID = "device_id";
    /** 属性名（column 形态属性表单值行） */
    public static final String TAG_PROPERTY = "property";
    /** 事件标识（事件表） */
    public static final String TAG_EVENT = "event";
    /** 事件数据的物模型数据类型名（事件表；可能缺失，缺失不落） */
    public static final String TAG_DATA_TYPE = "data_type";
    /** 命令类型（消息日志表） */
    public static final String TAG_COMMAND_TYPE = "command_type";

    // ===== 逻辑保留列（三表均有，属性名不得占用 —— 物模型校验上游兜底，策略写入再兜一层）=====
    public static final String FIELD_MESSAGE_ID = "message_id";
    /** 消息时间列（i64 毫秒）— 取消息报文 timestamp；与主时间列 _ts（TDengine 服务器落库时间）区分 */
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_DEVICE_NAME = "device_name";

    // ===== 主时间列（TDengine 主键列，行协议尾部时间戳落此列；查询恒按此列倒序）=====
    /** schemaless 自动建表时主时间列固定命名 _ts（实测 3.3.x：写 ts/查 ts 均报 Invalid column name），查询/解码统一用此常量 */
    public static final String COLUMN_TS = "_ts";

    // ===== 事件/日志行固定字段列（仅各自表内；字段数少无动态列，两布局形态一致）=====
    /** 事件类型 info/warning/error（事件表列，取物模型 EventMetadata.getType()） */
    public static final String FIELD_TYPE = "type";
    /** 事件数据 JSON 字符串（事件表列） */
    public static final String FIELD_DATA = "data";
    /** 方向 up/down（消息日志表列） */
    public static final String FIELD_DIRECTION = "direction";
    /** 消息内容 JSON 字符串（消息日志表列，整条消息全量序列化） */
    public static final String FIELD_CONTENT = "content";

    // ===== column 形态属性表单值类型列（每行恰落一个，其余列不落值；物理行其余类型列为 null）=====
    /** 整数类/BigInteger → long_value（BIGINT） */
    public static final String COLUMN_VALUE_LONG = "long_value";
    /** 字符/枚举/集合/对象 → string_value（字符串或紧凑 JSON，NCHAR） */
    public static final String COLUMN_VALUE_STRING = "string_value";
    /** 浮点类/BigDecimal → double_value（DOUBLE） */
    public static final String COLUMN_VALUE_DOUBLE = "double_value";
    /** Date → date_value（毫秒 i64） */
    public static final String COLUMN_VALUE_DATE = "date_value";
    /** Boolean → boolean_value */
    public static final String COLUMN_VALUE_BOOLEAN = "boolean_value";
    /** 单值类型列（列名即查询行键，亦作最新值查询取列；扫描取首个非空即为该行属性值） */
    public static final String[] COLUMN_VALUE_KEYS = {
            COLUMN_VALUE_LONG, COLUMN_VALUE_STRING, COLUMN_VALUE_DOUBLE, COLUMN_VALUE_DATE, COLUMN_VALUE_BOOLEAN};

    /** 动态列名 → 查询行键（驼峰转下划线小写：tempValue → temp_value）— 固定列名本身即行键（写读同一套命名），映射只服务属性名等动态列，通道出口与解码取值共用 */
    public static String rowKey(String columnName) {
        return columnName.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    /** 属性名是否占用保留列名（消息标识/消息时间/设备快照列，属性不得覆盖） */
    public static boolean isReservedField(String property) {
        return FIELD_MESSAGE_ID.equals(property)
                || FIELD_CREATE_TIME.equals(property)
                || FIELD_DEVICE_NAME.equals(property);
    }

    /** Java 值 → column 单值列名（值落哪个类型列由 Java 类型决定，与行协议类型分派对齐） */
    public static String valueColumnName(Object value) {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer
                || value instanceof Long || value instanceof BigInteger) {
            return COLUMN_VALUE_LONG;
        }
        if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            return COLUMN_VALUE_DOUBLE;
        }
        if (value instanceof Boolean) {
            return COLUMN_VALUE_BOOLEAN;
        }
        if (value instanceof Date) {
            return COLUMN_VALUE_DATE;
        }
        // 字符/枚举/集合/对象 → string_value（字符串值或紧凑 JSON，由行协议构建序列化）
        return COLUMN_VALUE_STRING;
    }
}
