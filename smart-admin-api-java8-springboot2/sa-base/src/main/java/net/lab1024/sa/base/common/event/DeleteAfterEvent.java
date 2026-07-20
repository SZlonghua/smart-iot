package net.lab1024.sa.base.common.event;

/**
 * 删除后事件（含物理删除和软删除）。
 * 通过 {@link #isPhysicalDelete()} 区分删除方式。
 *
 * @param <T> 实体类型
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public class DeleteAfterEvent<T> extends DataChangeEvent<T> {

    public DeleteAfterEvent(Object source, String entityClassName, String entitySimpleName,
                            String tableName, Long entityId, T beforeData,
                            T afterData, boolean physicalDelete, Class<?> entityType) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
                beforeData, afterData, physicalDelete, entityType);
    }
}
