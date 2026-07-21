package net.lab1024.sa.base.metadata.type;

import lombok.Data;

/**
 * Object 属性定义
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
@Data
public class ObjectProperty {

    /** 属性 ID */
    private String id;

    /** 属性名称 */
    private String name;

    /** 属性值类型 */
    private DataType valueType;
}
