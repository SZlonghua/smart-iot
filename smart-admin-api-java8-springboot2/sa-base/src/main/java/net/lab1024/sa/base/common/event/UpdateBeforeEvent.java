package net.lab1024.sa.base.common.event;

/**
 * 修改前事件
 *
 * @param <T> 实体类型
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public class UpdateBeforeEvent<T> extends WriteEvent<T> {

    public UpdateBeforeEvent(Object source, String entityClassName, String entitySimpleName,
                             String tableName, Long entityId, T beforeData,
                             T afterData, Class<?> entityType) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
                beforeData, afterData, entityType);
    }
}
