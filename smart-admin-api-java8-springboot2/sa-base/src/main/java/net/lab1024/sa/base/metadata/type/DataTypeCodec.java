package net.lab1024.sa.base.metadata.type;

import net.lab1024.sa.base.module.support.json.IJsonNode;

/**
 * 数据类型编解码器接口 —— 只负责序列化/反序列化
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public interface DataTypeCodec<T extends DataType> {

    /** 类型标识，如 "int" */
    String getTypeName();

    /** IJsonNode → DataType */
    T decode(IJsonNode node);

    /** DataType → IJsonNode */
    IJsonNode encode(T dataType);
}
