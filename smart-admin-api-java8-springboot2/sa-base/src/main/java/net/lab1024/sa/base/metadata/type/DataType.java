package net.lab1024.sa.base.metadata.type;

import net.lab1024.sa.base.metadata.Validatable;

/**
 * 数据类型接口
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public interface DataType extends Validatable {

    /** 类型标识 */
    String getType();
}
