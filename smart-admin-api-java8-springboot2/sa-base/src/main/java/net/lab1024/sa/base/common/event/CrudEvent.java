package net.lab1024.sa.base.common.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.time.LocalDateTime;

/**
 * CRUD 事件顶层抽象基类，泛型 T 为实体类型。
 * 实现 ResolvableTypeProvider，Spring 按 SaveAfterEvent&lt;DeviceEntity&gt; 精准匹配，无需 isEntityType 守卫。
 *
 * @param <T> 实体类型
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public abstract class CrudEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {

    /** 实体全限定类名 */
    private final String entityClassName;

    /** 实体简单类名 */
    private final String entitySimpleName;

    /** 数据库表名（从 @TableName 注解获取） */
    private final String tableName;

    /** 主键值（INSERT 前为 null） */
    private final Long entityId;

    /** 操作前数据：INSERT→null，UPDATE→旧实体，DELETE→删除前实体 */
    private final T beforeData;

    /** 操作后数据：INSERT→入库后实体，UPDATE→新实体，DELETE→null */
    private final T afterData;

    /** 是否为物理删除（true = DELETE SQL，false = 软删除 update deletedFlag=true） */
    private final boolean physicalDelete;

    /** 事件发生时间 */
    private final LocalDateTime eventTime;

    /** 实体类型 Class，用于 ResolvableTypeProvider 运行时泛型匹配 */
    private final Class<?> entityType;

    protected CrudEvent(Object source, String entityClassName, String entitySimpleName,
                        String tableName, Long entityId, T beforeData,
                        T afterData, boolean physicalDelete, Class<?> entityType) {
        super(source);
        this.entityClassName = entityClassName;
        this.entitySimpleName = entitySimpleName;
        this.tableName = tableName;
        this.entityId = entityId;
        this.beforeData = beforeData;
        this.afterData = afterData;
        this.physicalDelete = physicalDelete;
        this.eventTime = LocalDateTime.now();
        this.entityType = entityType;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(entityType));
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public String getEntitySimpleName() {
        return entitySimpleName;
    }

    public String getTableName() {
        return tableName;
    }

    public Long getEntityId() {
        return entityId;
    }

    public T getBeforeData() {
        return beforeData;
    }

    public T getAfterData() {
        return afterData;
    }

    public boolean isPhysicalDelete() {
        return physicalDelete;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

}
