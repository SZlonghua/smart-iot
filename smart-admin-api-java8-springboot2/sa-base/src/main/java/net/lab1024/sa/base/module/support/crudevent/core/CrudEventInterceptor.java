package net.lab1024.sa.base.module.support.crudevent.core;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.event.*;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CRUD 事件拦截器，拦截所有 MyBatis INSERT/UPDATE/DELETE 操作，自动发布对应的事件。
 * 支持逐条事件、操作前/后数据、物理删除与软删除区分、表级过滤。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
@Slf4j
@Component
@Intercepts(@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}))
public class CrudEventInterceptor implements Interceptor {

    private static final ThreadLocal<Boolean> INTERCEPTING = ThreadLocal.withInitial(() -> false);

    private static final String DELETED_FLAG_FIELD = "deletedFlag";

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Lazy
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired(required = false)
    private CrudEventConfig config;

    private final Map<Class<?>, EntityMetadata> metadataCache = new ConcurrentHashMap<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (Boolean.TRUE.equals(INTERCEPTING.get())) {
            return invocation.proceed();
        }

        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        SqlCommandType commandType = ms.getSqlCommandType();
        if (commandType != SqlCommandType.INSERT
                && commandType != SqlCommandType.UPDATE
                && commandType != SqlCommandType.DELETE) {
            return invocation.proceed();
        }

        if (parameter == null) {
            return invocation.proceed();
        }

        INTERCEPTING.set(true);
        try {
            switch (commandType) {
                case INSERT:
                    return handleInsert(invocation, ms, parameter);
                case UPDATE:
                    return handleUpdate(invocation, ms, parameter);
                case DELETE:
                    return handleDelete(invocation, ms, parameter);
                default:
                    return invocation.proceed();
            }
        } finally {
            INTERCEPTING.set(false);
        }
    }

    private EventContext resolveContext(MappedStatement ms) {
        Class<?> entityClass = deriveEntityClass(ms);
        if (entityClass == null) {
            return null;
        }
        EntityMetadata metadata = getOrCreateMetadata(entityClass);
        if (shouldExclude(metadata.getTableName(), entityClass.getName())) {
            return null;
        }
        return new EventContext(entityClass, metadata, sqlSessionFactory);
    }

    // ==================== 操作处理 ====================

    private Object handleInsert(Invocation invocation, MappedStatement ms, Object parameter) throws Throwable {
        EventContext ctx = resolveContext(ms);
        if (ctx == null) {
            return invocation.proceed();
        }
        Object entity = ctx.unwrap(parameter);

        eventPublisher.publishEvent(new SaveBeforeEvent<>(entity, ctx.getClassName(), ctx.getSimpleName(),
                ctx.tableName(), null, null, entity, ctx.getEntityClass()));
        Object result = invocation.proceed();
        eventPublisher.publishEvent(new SaveAfterEvent<>(entity, ctx.getClassName(), ctx.getSimpleName(),
                ctx.tableName(), ctx.extractEntityId(entity), null, entity, ctx.getEntityClass()));

        return result;
    }

    private Object handleUpdate(Invocation invocation, MappedStatement ms, Object parameter) throws Throwable {
        EventContext ctx = resolveContext(ms);
        if (ctx == null) {
            return invocation.proceed();
        }
        Object entity = ctx.unwrap(parameter);

        Long entityId = ctx.extractEntityId(entity);
        Object beforeData = ctx.queryBeforeData(ms.getId(), entityId);

        if (ctx.detectSoftDelete(entity)) {
            eventPublisher.publishEvent(new DeleteBeforeEvent<>(entity, ctx.getClassName(), ctx.getSimpleName(),
                    ctx.tableName(), entityId, beforeData, entity, false, ctx.getEntityClass()));
            Object result = invocation.proceed();
            eventPublisher.publishEvent(new DeleteAfterEvent<>(entity, ctx.getClassName(), ctx.getSimpleName(),
                    ctx.tableName(), entityId, beforeData, entity, false, ctx.getEntityClass()));
            return result;
        }

        eventPublisher.publishEvent(new UpdateBeforeEvent<>(entity, ctx.getClassName(), ctx.getSimpleName(),
                ctx.tableName(), entityId, beforeData, entity, ctx.getEntityClass()));
        Object result = invocation.proceed();
        eventPublisher.publishEvent(new UpdateAfterEvent<>(entity, ctx.getClassName(), ctx.getSimpleName(),
                ctx.tableName(), entityId, beforeData, entity, ctx.getEntityClass()));

        return result;
    }

    private Object handleDelete(Invocation invocation, MappedStatement ms, Object parameter) throws Throwable {
        EventContext ctx = resolveContext(ms);
        if (ctx == null) {
            return invocation.proceed();
        }

        if (parameter instanceof Collection) {
            return handleBatchDelete(invocation, ms, (Collection<?>) parameter, ctx);
        }

        Long entityId = ctx.extractDeleteId(parameter);
        Object beforeData = ctx.queryBeforeData(ms.getId(), entityId);

        eventPublisher.publishEvent(new DeleteBeforeEvent<>(parameter, ctx.getClassName(), ctx.getSimpleName(),
                ctx.tableName(), entityId, beforeData, null, true, ctx.getEntityClass()));
        Object result = invocation.proceed();
        eventPublisher.publishEvent(new DeleteAfterEvent<>(parameter, ctx.getClassName(), ctx.getSimpleName(),
                ctx.tableName(), entityId, beforeData, null, true, ctx.getEntityClass()));

        return result;
    }

    private Object handleBatchDelete(Invocation invocation, MappedStatement ms,
                                     Collection<?> idList, EventContext ctx) throws Throwable {
        String selectMsId = ctx.deriveSelectMsId(ms.getId());

        for (Object idObj : idList) {
            Long entityId = toLong(idObj);
            Object beforeData = ctx.queryBeforeData(selectMsId, entityId);
            eventPublisher.publishEvent(new DeleteBeforeEvent<>(idObj, ctx.getClassName(), ctx.getSimpleName(),
                    ctx.tableName(), entityId, beforeData, null, true, ctx.getEntityClass()));
        }

        Object result = invocation.proceed();

        for (Object idObj : idList) {
            Long entityId = toLong(idObj);
            eventPublisher.publishEvent(new DeleteAfterEvent<>(idObj, ctx.getClassName(), ctx.getSimpleName(),
                    ctx.tableName(), entityId, null, null, true, ctx.getEntityClass()));
        }

        return result;
    }

    // ==================== 辅助方法 ====================

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    /**
     * 从 MappedStatement ID 推导实体类。
     * 直接读取 DAO 接口继承 BaseMapper&lt;Entity&gt; 的泛型参数，无需猜测包名。
     */
    private Class<?> deriveEntityClass(MappedStatement ms) {
        String msId = ms.getId();
        int lastDot = msId.lastIndexOf('.');
        String daoFqcn = msId.substring(0, lastDot);

        try {
            Class<?> mapperClass = Class.forName(daoFqcn);
            for (java.lang.reflect.Type iface : mapperClass.getGenericInterfaces()) {
                if (iface instanceof java.lang.reflect.ParameterizedType) {
                    java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) iface;
                    if (pt.getRawType() == com.baomidou.mybatisplus.core.mapper.BaseMapper.class) {
                        java.lang.reflect.Type typeArg = pt.getActualTypeArguments()[0];
                        if (typeArg instanceof Class) {
                            return (Class<?>) typeArg;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        log.warn("CRUD事件拦截器无法推导实体类: msId={}", msId);
        return null;
    }

    private EntityMetadata getOrCreateMetadata(Class<?> entityClass) {
        return metadataCache.computeIfAbsent(entityClass, this::resolveMetadata);
    }

    private EntityMetadata resolveMetadata(Class<?> entityClass) {
        Field idField = null;
        Field deletedFlagField = null;
        String tableName = null;

        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (idField == null && field.isAnnotationPresent(TableId.class)) {
                    idField = field;
                    idField.setAccessible(true);
                }
                if (deletedFlagField == null
                        && DELETED_FLAG_FIELD.equals(field.getName())
                        && field.getType() == Boolean.class) {
                    deletedFlagField = field;
                    deletedFlagField.setAccessible(true);
                }
            }
            current = current.getSuperclass();
        }

        TableName tableNameAnno = entityClass.getAnnotation(TableName.class);
        if (tableNameAnno != null) {
            tableName = tableNameAnno.value();
        } else {
            String simpleName = entityClass.getSimpleName();
            if (simpleName.endsWith("Entity")) {
                simpleName = simpleName.substring(0, simpleName.length() - 6);
            }
            tableName = camelToSnake(simpleName);
        }

        return new EntityMetadata(idField, tableName, deletedFlagField);
    }

    private String camelToSnake(String camel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean shouldExclude(String tableName, String entityClassName) {
        if (config == null) {
            return false;
        }
        return config.shouldExclude(tableName, entityClassName);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 无配置项
    }
}
