package net.lab1024.sa.base.metadata;

import net.lab1024.sa.base.metadata.type.DataType;

/**
 * 功能参数元数据接口
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public interface FunctionParamMetadata extends Metadata {

    String getId();
    String getName();
    DataType getValueType();
    boolean isRequired();
}
