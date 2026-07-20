package net.lab1024.sa.base.common.event;

/**
 * "数据变更事件" 抽象中间层 — 涵盖所有 6 种事件（新增 + 修改 + 删除）。
 * 监听器声明 DataChangeEvent&lt;DeviceEntity&gt; 即可监听该实体的所有变更。
 *
 * @param <T> 实体类型
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public abstract class DataChangeEvent<T> extends CrudEvent<T> {

    protected DataChangeEvent(Object source, String entityClassName, String entitySimpleName,
                              String tableName, Long entityId, T beforeData,
                              T afterData, boolean physicalDelete, Class<?> entityType) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
                beforeData, afterData, physicalDelete, entityType);
    }
}
