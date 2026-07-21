package net.lab1024.sa.base.metadata;

import net.lab1024.sa.base.metadata.type.DataType;

/**
 * 属性元数据接口
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public interface PropertyMetadata extends Metadata, Jsonable {

    String getId();
    String getName();
    DataType getValueType();
    String getAccessMode();
    String getDescription();
}
