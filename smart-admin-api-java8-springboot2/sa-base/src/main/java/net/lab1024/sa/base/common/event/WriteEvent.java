package net.lab1024.sa.base.common.event;

/**
 * "写入事件" 抽象中间层 — 仅涵盖新增 + 修改（共 4 种事件），不包含删除。
 * 监听器声明 WriteEvent&lt;DeviceEntity&gt; 即可监听写入操作，自动排除删除。
 *
 * @param <T> 实体类型
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
public abstract class WriteEvent<T> extends DataChangeEvent<T> {

    protected WriteEvent(Object source, String entityClassName, String entitySimpleName,
                         String tableName, Long entityId, T beforeData,
                         T afterData, Class<?> entityType) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
                beforeData, afterData, false, entityType);
    }
}
