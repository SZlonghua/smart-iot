package net.lab1024.sa.base.metadata.type;

import net.lab1024.sa.base.metadata.Validatable;

import java.util.List;

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

    /** 值校验 — 校验传入值是否符合该类型约束（int 范围 / bool 类型 / enum 枚举值等），返回错误列表（空 = 合法） */
    List<String> validateValue(Object value);
}
