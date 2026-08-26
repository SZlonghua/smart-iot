package net.lab1024.sa.base.metadata.type;

import net.lab1024.sa.base.metadata.Validatable;

import java.util.Collections;
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

    /** 校验单个值 — 预留：默认返回空列表（各类型实现后续按物模型校验设计.md 落地，本阶段不改动各 *DataType） */
    default List<String> validateValue(Object value) {
        return Collections.emptyList();
    }
}
