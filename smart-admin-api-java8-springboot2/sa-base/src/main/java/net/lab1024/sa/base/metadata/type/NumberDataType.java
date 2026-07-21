package net.lab1024.sa.base.metadata.type;

import lombok.Data;

/**
 * 数值类型抽象基类
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
@Data
public abstract class NumberDataType implements DataType {

    /** 单位 */
    private String unit;
}
