package net.lab1024.sa.base.storage;

/**
 * 设备消息存储策略 ID 常量 — 产品经 {@link net.lab1024.sa.base.device.DeviceProductOperator#getStoragePolicy()}
 * 选定策略，设备操作对象按 ID 从 {@link StorageStrategyRegistry} 取实现
 * （{@link net.lab1024.sa.base.device.DeviceOperator#getStorageStrategy()}；将来 influxdb / clickhouse /
 * elasticsearch 策略再加常量并实现对应 {@link StorageStrategy}）。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
public final class StoragePolicy {

    private StoragePolicy() {
    }

    /** TDengine 行存储（多列）：属性一条消息一行、属性摊平为列；事件/日志一行一条消息 */
    public static final String TDENGINE_ROW = "tdengine-row";

    /** TDengine 单值列存储：属性表每属性一行（tags={device_id, property} + 固定类型值列），事件/日志表与 row 同形态 */
    public static final String TDENGINE_COLUMN = "tdengine-column";

}
