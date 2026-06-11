package net.lab1024.sa.base.metadata;

import net.lab1024.sa.base.metadata.type.DataType;

/**
 * 事件元数据接口
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public interface EventMetadata extends Metadata, Jsonable {

    String getId();
    String getName();
    String getEventType();
    DataType getValueType();
    String getDescription();
}
