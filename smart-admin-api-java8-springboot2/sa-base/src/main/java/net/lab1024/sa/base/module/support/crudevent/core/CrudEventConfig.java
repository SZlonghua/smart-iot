package net.lab1024.sa.base.module.support.crudevent.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * CRUD 事件配置。
 * 支持总开关、按表名/实体类/表名前缀排除不需要发送事件的表。
 * 在 application.yaml 中以 crud-event 为前缀配置。
 *
 * @Author 廖涛
 * @Date 2026/07/19
 * @Copyright 1024创新实验室
 */
@Data
@Component
@ConfigurationProperties(prefix = "crud-event")
public class CrudEventConfig {

    /** 总开关，默认 true，关闭后所有表都不发送事件 */
    private boolean enabled = true;

    /** 排除的表名集合，如 "t_employee" */
    private Set<String> excludeTableNames;

    /** 排除的实体类全限定名集合 */
    private Set<String> excludeEntityClassNames;

    /** 排除的表名前缀集合，如 "t_sys_" */
    private Set<String> excludeTablePrefixes;

    /**
     * 判断指定的表/实体是否应该被排除（不发事件）
     */
    public boolean shouldExclude(String tableName, String entityClassName) {
        if (!enabled) {
            return true;
        }
        if (tableName == null && entityClassName == null) {
            return false;
        }
        // 按表名排除
        if (excludeTableNames != null && tableName != null && excludeTableNames.contains(tableName)) {
            return true;
        }
        // 按实体类全限定名排除
        if (excludeEntityClassNames != null && entityClassName != null && excludeEntityClassNames.contains(entityClassName)) {
            return true;
        }
        // 按表名前缀排除
        if (excludeTablePrefixes != null && tableName != null) {
            for (String prefix : excludeTablePrefixes) {
                if (tableName.startsWith(prefix)) {
                    return true;
                }
            }
        }
        return false;
    }
}
