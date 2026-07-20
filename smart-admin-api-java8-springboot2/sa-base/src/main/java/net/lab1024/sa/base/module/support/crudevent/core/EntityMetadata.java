package net.lab1024.sa.base.module.support.crudevent.core;

import java.lang.reflect.Field;

/**
 * 实体反射元数据缓存，避免热路径上重复反射。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public class EntityMetadata {

    /** 标注 @TableId 的字段（已 setAccessible） */
    private final Field idField;

    /** @TableName 注解的 value */
    private final String tableName;

    /** Boolean deletedFlag 字段，没有则为 null */
    private final Field deletedFlagField;

    public EntityMetadata(Field idField, String tableName, Field deletedFlagField) {
        this.idField = idField;
        this.tableName = tableName;
        this.deletedFlagField = deletedFlagField;
    }

    public Field getIdField() {
        return idField;
    }

    public String getTableName() {
        return tableName;
    }

    public Field getDeletedFlagField() {
        return deletedFlagField;
    }
}
