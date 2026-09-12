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
    /** 事件类型：info=信息 / warning=告警 / error=故障 */
    String getType();
    DataType getValueType();
    String getDescription();
}
