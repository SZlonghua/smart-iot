package net.lab1024.sa.base.storage.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 事件上报业务模型（继承 {@link StorageData} 公共字段）— 原始消息存储，不等同告警日志
 * （告警规则引擎后续实现，规则触发后再写告警日志）。
 * <p>
 * type/dataType 由监听器取产品物模型回填：type 取物模型 EventMetadata.getType()（info/warning/error），
 * dataType 取物模型 valueType 的 type 名（如 int/object）；data 为事件数据 JSON 字符串
 * （数据类型由物模型 valueType 动态定义，统一 JSON 字符串存储）。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EventData extends StorageData {

    /** 事件标识（id，tag） */
    private String event;

    /** 事件类型 info/warning/error（列，取物模型 EventMetadata.getType()） */
    private String type;

    /** 事件数据的物模型数据类型名（tag，取物模型 valueType 的 type 名） */
    private String dataType;

    /** 事件数据 JSON 字符串（紧凑序列化） */
    private String data;
}
