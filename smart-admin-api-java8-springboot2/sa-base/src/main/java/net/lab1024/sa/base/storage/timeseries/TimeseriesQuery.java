package net.lab1024.sa.base.storage.timeseries;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 时序查询 SQL 构建器 — 查询条件、查询列、分页状态与语句骨架收敛于本对象（时序家族查询方言统一出口）：
 * 条件链式追加（{@link #eq} 列等值 / {@link #in} 列集合包含 / {@link #isNotNull} 非空 /
 * {@link #fromTime} {@link #toTime} 主时间列毫秒时间范围），查询列链式追加（{@link #columns}），
 * 分页链式设置（{@link #limit} {@link #offset}），按查询形态出 SQL —— {@link #pageSql} 分页数据（带分页状态）、
 * {@link #countSql} 总数与 {@link #firstSql} 最新单行（均不含分页状态），排序恒为主时间列倒序。
 * <p>
 * 空值判空内敛于各条件方法（空白值/null 自动跳过），调用方无需外在判空即可流式链写；
 * 条件以追加序 AND 连接、空条件组不渲染 WHERE 段。三形态输出与查询解码约定（_ts/device_id 等行键）
 * 逐字节稳定，策略测试据此做精确断言。
 * <p>
 * 标识符（表名/列名/别名）一律 TDengine 反引号引用（{@link #identifier}）：schemaless 按原始形态建列
 * （属性名 `11`/`tempValue` 原样落列），裸标识符会被解析器小写化（Invalid column name）或当作数值字面量
 * （`SELECT 11` 返回常量 11 而非该列值）。
 *
 * @Author 廖涛
 * @Date 2026/09/09
 * @Copyright 1024创新实验室
 */
public final class TimeseriesQuery {

    /** count 查询别名列名（{@link #countSql} 的 AS total；总数解码按此键取值） */
    public static final String COUNT_ALIAS = "total";

    private final String table;
    private final List<String> conditions = new ArrayList<>(4);

    /** 查询列（链式状态；{@link #firstSql} 取用，恒追加主时间列（_ts）供行解码） */
    private final List<String> columns = new ArrayList<>(2);

    /** 每页条数（分页链式状态；<=0 表示仅 count 不查数据） */
    private int limit;

    /** 起始偏移（分页链式状态；= (pageNum-1)*pageSize） */
    private long offset;

    private TimeseriesQuery(String table) {
        this.table = table;
    }

    public static TimeseriesQuery from(String table) {
        return new TimeseriesQuery(table);
    }

    /** 列等值条件 — deviceId/event/property 等 tag 或文本列，值单引号翻倍转义；空白值自动跳过 */
    public TimeseriesQuery eq(String column, String value) {
        if (StringUtils.isNotBlank(value)) {
            conditions.add(identifier(column) + " = " + quote(value));
        }
        return this;
    }

    /** 列集合包含条件 — 命令日志按类型集合过滤（类型与回复类型成对传入）；空集合/null 自动跳过 */
    public TimeseriesQuery in(String column, Collection<String> values) {
        if (values != null && !values.isEmpty()) {
            conditions.add(identifier(column) + " IN (" + values.stream().map(TimeseriesQuery::quote).collect(Collectors.joining(",")) + ")");
        }
        return this;
    }

    /** 列非空条件 — 动态属性列最新值定位兜底 */
    public TimeseriesQuery isNotNull(String column) {
        conditions.add(identifier(column) + " IS NOT NULL");
        return this;
    }

    /** 查询列链式追加 — 最新值查询取用的列（动态属性列 / 单值类型列），出 SQL 时恒追加主时间列（_ts） */
    public TimeseriesQuery columns(String... columns) {
        for (String column : columns) {
            this.columns.add(identifier(column));
        }
        return this;
    }

    /** 主时间列起始时间（毫秒，含边界）；null 自动跳过 */
    public TimeseriesQuery fromTime(Long startTime) {
        if (startTime != null) {
            conditions.add(identifier(TimeseriesLayout.COLUMN_TS) + " >= " + startTime);
        }
        return this;
    }

    /** 主时间列结束时间（毫秒，含边界）；null 自动跳过 */
    public TimeseriesQuery toTime(Long endTime) {
        if (endTime != null) {
            conditions.add(identifier(TimeseriesLayout.COLUMN_TS) + " <= " + endTime);
        }
        return this;
    }

    /** 每页条数 — 分页链式设置（仅作用于 {@link #pageSql}），<=0 表示仅 count 不查数据 */
    public TimeseriesQuery limit(int limit) {
        this.limit = limit;
        return this;
    }

    /** 起始偏移 — 分页链式设置（= (pageNum-1)*pageSize） */
    public TimeseriesQuery offset(long offset) {
        this.offset = offset;
        return this;
    }

    public int getLimit() {
        return limit;
    }

    public long getOffset() {
        return offset;
    }

    /** 分页数据 SQL — SELECT * + 条件 + 主时间列倒序 + LIMIT/OFFSET（分页状态由 {@link #limit}/{@link #offset} 链式设置） */
    public String pageSql() {
        return "SELECT * FROM " + identifier(table) + whereClause() + " ORDER BY " + identifier(TimeseriesLayout.COLUMN_TS) + " DESC LIMIT " + limit + " OFFSET " + offset;
    }

    /** 最新单行 SQL — 链式列 + 主时间列（_ts）+ 条件 + 倒序 LIMIT 1（无 OFFSET 段） */
    public String firstSql() {
        List<String> selectColumns = new ArrayList<>(columns);
        selectColumns.add(identifier(TimeseriesLayout.COLUMN_TS));
        return "SELECT " + String.join(", ", selectColumns) + " FROM " + identifier(table) + whereClause()
                + " ORDER BY " + identifier(TimeseriesLayout.COLUMN_TS) + " DESC LIMIT 1";
    }

    /** 总数 SQL — SELECT COUNT(*) AS total + 条件 */
    public String countSql() {
        return "SELECT COUNT(*) AS " + identifier(COUNT_ALIAS) + " FROM " + identifier(table) + whereClause();
    }

    /** WHERE 段 — 条件组 AND 连接（追加序）；空条件组不渲染 */
    private String whereClause() {
        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    /**
     * 标识符引用 — TDengine 反引号：schemaless 建列保留原始形态（属性名 `11`/`tempValue` 原样落列），
     * 裸标识符会被解析器小写化（Invalid column name）或当作数值字面量（`SELECT 11` 返回常量）；
     * 标识符内的反引号双写转义
     */
    private static String identifier(String name) {
        return "`" + name.replace("`", "``") + "`";
    }

    /** SQL 字符串字面量（单引号包裹） */
    private static String quote(String literal) {
        return "'" + escape(literal) + "'";
    }

    /** 字符串字面量转义（单引号翻倍） */
    private static String escape(String literal) {
        return literal.replace("'", "''");
    }
}
