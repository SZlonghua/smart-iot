package net.lab1024.sa.base.module.support.crudevent.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * CRUD 事件上下文 — 封装实体类、元数据等解析后的基础信息，
 * 并提供 extractEntityId / detectSoftDelete / queryBeforeData / deriveSelectMsId 方法。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
@Slf4j
public class EventContext {
    @Getter
    private final Class<?> entityClass;
    private final EntityMetadata metadata;
    @Getter
    private final String className;
    @Getter
    private final String simpleName;
    private final SqlSessionFactory sqlSessionFactory;

    public EventContext(Class<?> entityClass, EntityMetadata metadata, SqlSessionFactory sqlSessionFactory) {
        this.entityClass = entityClass;
        this.metadata = metadata;
        this.className = entityClass.getName();
        this.simpleName = entityClass.getSimpleName();
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public String tableName() {
        return metadata.getTableName();
    }

    /**
     * 从实体对象中提取主键值
     */
    public Long extractEntityId(Object entity) {
        if (entity == null) {
            return null;
        }
        Field idField = metadata.getIdField();
        if (idField == null) {
            return null;
        }
        try {
            Object value = idField.get(entity);
            return value instanceof Number ? ((Number) value).longValue() : null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * 从 DELETE 参数中提取主键值。Number 直接转 Long；Map（ParamMap 包装）返回 null。
     */
    public Long extractDeleteId(Object parameter) {
        if (parameter instanceof Number) {
            return ((Number) parameter).longValue();
        }
        // 自定义删除语句不支持 如下:
        /*delete
        from t_login_fail
        where user_id = #{userId}
        and user_type = #{userType}*/
        if (parameter instanceof Map) {
            return null;
        }
        return extractEntityId(parameter);
    }

    /**
     * 检测是否为软删除（UPDATE 且 deletedFlag=true）
     */
    public boolean detectSoftDelete(Object entity) {
        Field deletedFlagField = metadata.getDeletedFlagField();
        if (deletedFlagField == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(deletedFlagField.get(entity));
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    /**
     * 通过独立 SqlSession 查询操作前旧数据
     */
    public Object queryBeforeData(String msId, Long entityId) {
        if (entityId == null) {
            return null;
        }
        String selectMsId = deriveSelectMsId(msId);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectOne(selectMsId, entityId);
        } catch (Exception e) {
            log.warn("CRUD事件拦截器查询beforeData失败: msId={}, entityId={}", selectMsId, entityId);
            return null;
        }
    }

    /**
     * 从 MyBatis ParamMap 中提取真正的 entity。
     * useActualParamName=true 时单参数被包装为 {"entity": entity, "param1": entity}
     */
    public Object unwrap(Object parameter) {
        if (parameter instanceof java.util.Map) {
            for (Object value : ((java.util.Map<?, ?>) parameter).values()) {
                if (value != null && entityClass.isInstance(value)) {
                    return value;
                }
            }
        }
        return parameter;
    }

    /**
     * 从 MappedStatement ID 推导 selectById 语句 ID
     * 例：xxx.DeviceDao.updateById → xxx.DeviceDao.selectById
     */
    public String deriveSelectMsId(String msId) {
        int lastDot = msId.lastIndexOf('.');
        if (lastDot > 0) {
            return msId.substring(0, lastDot) + ".selectById";
        }
        return msId;
    }
}
