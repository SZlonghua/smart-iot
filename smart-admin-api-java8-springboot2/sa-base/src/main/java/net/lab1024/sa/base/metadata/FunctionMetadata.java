package net.lab1024.sa.base.metadata;

import net.lab1024.sa.base.metadata.type.DataType;

import java.util.List;

/**
 * 功能元数据接口
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public interface FunctionMetadata extends Metadata, Jsonable {

    String getId();
    String getName();
    boolean isAsync();
    List<? extends FunctionParamMetadata> getInputs();
    DataType getOutput();
    String getDescription();
}
