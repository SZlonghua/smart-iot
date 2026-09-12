package net.lab1024.sa.base.storage.timeseries;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.storage.StorageStrategy;
import net.lab1024.sa.base.storage.model.CommandLogQuery;
import net.lab1024.sa.base.storage.model.EventData;
import net.lab1024.sa.base.storage.model.EventHistoryQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyValue;
import net.lab1024.sa.base.storage.model.MessageLogData;
import net.lab1024.sa.base.storage.model.PropertyData;
import net.lab1024.sa.base.storage.model.PropertyHistoryQuery;
import net.lab1024.sa.base.storage.model.StorageData;
import net.lab1024.sa.base.storage.model.TimeseriesRow;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 时序存储策略抽象基类 — 承载两布局共有的行形态逻辑，差异收敛在行/单值列子类。
 * <p>
 * 数据形态分层（DDD：布局差异与后端方言差异两轴正交）：
 * ①本类：事件/消息日志两张表在行与单值列布局下形态完全一致（固定字段、无动态列）→ 事件/日志的
 * 落库行协议、条件分页 SQL、查询行解码与 PageResult 组装全部在本类实现一次，两布局共用；
 * ②行/单值列差异只发生在属性表（动态属性列 vs 每属性一行单值列）→ 属性写与属性查询
 * （历史/最新值）留为抽象方法由 {@link TimeseriesRowStorageStrategy} /
 * {@link TimeseriesColumnStorageStrategy} 实现；
 * ③具体库方言（建库/物理写入/查询执行）经 {@link TimeseriesChannel} 注入，叶子策略（如
 * TDengine 行/单值列）只做构造注入与策略 ID 声明。
 * <p>
 * 消息模型 → 行数据由各布局策略自转换（不入参共享转换器，见各类 saveProperty）；
 * 查询 SQL 按业务条件组装（device_id tag 等值 + 业务条件 + ts 时间范围含边界，时间倒序 + LIMIT/OFFSET），
 * 查询行经行读取器 {@link TimeseriesRow} 解码（行键取 {@link TimeseriesLayout} 列名常量），在本类与子类完成。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Slf4j
public abstract class TimeseriesStorageStrategy implements StorageStrategy {

    /** 物理读写通道（后端方言） */
    protected final TimeseriesChannel channel;

    protected TimeseriesStorageStrategy(TimeseriesChannel channel) {
        this.channel = channel;
    }

    // ===== 保存：唯一写入口 save(StorageData) 按实际子类分派 =====

    /**
     * 消息落库 — 设备/产品维度缺失（子设备会话未找到等）或未知消息类型仅记日志跳过，不阻断消息流
     */
    @Override
    public final void save(StorageData data) {
        if (data == null || StringUtils.isBlank(data.getDeviceId()) || StringUtils.isBlank(data.getProductId())) {
            log.debug("消息缺少设备/产品维度，跳过存储: {}", data == null ? null : data.getDeviceId());
            return;
        }
        if (data instanceof PropertyData) {
            saveProperty((PropertyData) data);
            return;
        }
        if (data instanceof EventData) {
            saveEvent((EventData) data);
            return;
        }
        if (data instanceof MessageLogData) {
            saveMessageLog((MessageLogData) data);
            return;
        }
        log.warn("未支持的消息数据类型 [{}]，跳过存储: deviceId={}", data.getClass().getSimpleName(), data.getDeviceId());
    }

    /** 属性落库 — 行形态差异（摊平列 vs 单值行）由布局子类实现 */
    protected abstract void saveProperty(PropertyData data);

    /** 事件落库 — 事件表固定字段无动态列，两布局同一行形态：tags={device_id,event,data_type}，fields=保留列+type/data */
    private void saveEvent(EventData data) {
        writeLines(Collections.singletonList(SmartPoint.builder()
                .table(timeseriesTable(TimeseriesLayout.TABLE_PREFIX_EVENT, data.getProductId()))
                .tag(TimeseriesLayout.TAG_DEVICE_ID, data.getDeviceId())
                .tag(TimeseriesLayout.TAG_EVENT, data.getEvent())
                .tag(TimeseriesLayout.TAG_DATA_TYPE, data.getDataType())
                .field(TimeseriesLayout.FIELD_MESSAGE_ID, data.getMessageId())
                .field(TimeseriesLayout.FIELD_CREATE_TIME, data.getTimestamp())
                .field(TimeseriesLayout.FIELD_DEVICE_NAME, data.getDeviceName())
                .field(TimeseriesLayout.FIELD_TYPE, data.getType())
                .field(TimeseriesLayout.FIELD_DATA, data.getData())
                .build()
                .toLineProtocol()));
    }

    /** 命令日志落库 — 日志表固定字段无动态列，两布局同一行形态：tags={device_id,command_type}，fields=保留列+direction/content */
    private void saveMessageLog(MessageLogData data) {
        writeLines(Collections.singletonList(SmartPoint.builder()
                .table(timeseriesTable(TimeseriesLayout.TABLE_PREFIX_MESSAGE_LOG, data.getProductId()))
                .tag(TimeseriesLayout.TAG_DEVICE_ID, data.getDeviceId())
                .tag(TimeseriesLayout.TAG_COMMAND_TYPE, data.getCommandType())
                .field(TimeseriesLayout.FIELD_MESSAGE_ID, data.getMessageId())
                .field(TimeseriesLayout.FIELD_CREATE_TIME, data.getTimestamp())
                .field(TimeseriesLayout.FIELD_DEVICE_NAME, data.getDeviceName())
                .field(TimeseriesLayout.FIELD_DIRECTION, data.getDirection())
                .field(TimeseriesLayout.FIELD_CONTENT, data.getContent())
                .build()
                .toLineProtocol()));
    }

    // ===== 事件/日志分页查询（两布局同形态，本类一次实现）=====

    @Override
    public PageResult<EventData> queryEventHistory(EventHistoryQuery query) {
        // 空白 event 由构建器 eq 内部跳过；分页状态（limit/offset）随查询对象链式设置
        TimeseriesQuery tsQuery = TimeseriesQuery.from(timeseriesTable(TimeseriesLayout.TABLE_PREFIX_EVENT, query.getProductId()))
                .eq(TimeseriesLayout.TAG_DEVICE_ID, query.getDeviceId())
                .fromTime(query.getStartTime())
                .toTime(query.getEndTime())
                .eq(TimeseriesLayout.TAG_EVENT, query.getEvent())
                .limit(query.getPageSize())
                .offset((long) (query.getPageNum() - 1) * query.getPageSize());
        return queryPage(tsQuery, TimeseriesStorageStrategy::eventRowToData);
    }

    @Override
    public PageResult<MessageLogData> queryCommandLogHistory(CommandLogQuery query) {
        // direction/deviceName 空白由构建器 eq 内部跳过、commandTypes 空集合由 in 内部跳过；分页状态（limit/offset）随查询对象链式设置
        TimeseriesQuery tsQuery = TimeseriesQuery.from(timeseriesTable(TimeseriesLayout.TABLE_PREFIX_MESSAGE_LOG, query.getProductId()))
                .eq(TimeseriesLayout.TAG_DEVICE_ID, query.getDeviceId())
                .fromTime(query.getStartTime())
                .toTime(query.getEndTime())
                .eq(TimeseriesLayout.FIELD_DIRECTION, query.getDirection())
                .eq(TimeseriesLayout.FIELD_DEVICE_NAME, query.getDeviceName())
                .in(TimeseriesLayout.TAG_COMMAND_TYPE, query.getCommandTypes())
                .limit(query.getPageSize())
                .offset((long) (query.getPageNum() - 1) * query.getPageSize());
        return queryPage(tsQuery, TimeseriesStorageStrategy::messageLogRowToData);
    }

    // ===== 属性查询（行形态差异）=====

    @Override
    public abstract PageResult<PropertyData> queryPropertyHistory(PropertyHistoryQuery query);

    @Override
    public abstract Map<String, LatestPropertyValue> queryLatestPropertyValues(LatestPropertyQuery query);

    // ===== 共享物理写入/查询助手（布局子类复用）=====

    /** 批量写入行协议（空批不写 — 单值行全部被过滤时正常触发；单行写入恒有 deviceId tag 而非空） */
    protected void writeLines(List<String> lines) {
        if (!lines.isEmpty()) {
            channel.writeLines(lines);
        }
    }

    /** 逻辑表名 = 前缀 + 产品 id */
    protected static String timeseriesTable(String prefix, String productId) {
        return prefix + productId;
    }

    // ===== 分页两查 + 行解码（基类事件/日志与子类属性历史共用）=====

    /** 分页查询 — count + 数据两查（同一查询对象复用，分页状态取自其 limit/offset）；总数 0（含表不存在）或 limit<=0 短路不查数据 */
    protected <T> PageResult<T> queryPage(TimeseriesQuery tsQuery, Function<TimeseriesRow, T> rowMapper) {
        long total = countWhere(tsQuery);

        List<T> list = new ArrayList<>();
        if (total > 0 && tsQuery.getLimit() > 0) {
            for (Map<String, Object> row : channel.executeQuery(tsQuery.pageSql())) {
                list.add(rowMapper.apply(new TimeseriesRow(row)));
            }
        }

        return pageResult(tsQuery, total, list);
    }

    private long countWhere(TimeseriesQuery tsQuery) {
        // 表不存在 → 通道返回空列表 → 0（产品尚无数据视为无记录）
        List<Map<String, Object>> rows = channel.executeQuery(tsQuery.countSql());
        return rows.isEmpty() ? 0L : new TimeseriesRow(rows.get(0)).getLong(TimeseriesQuery.COUNT_ALIAS);
    }

    /** 事件查询行解码 — 时间为消息上报时间，取消息时间列 create_time（主时间列 _ts 为 TDengine 服务器落库时间，不参与解码） */
    private static EventData eventRowToData(TimeseriesRow row) {
        EventData data = new EventData();
        data.setDeviceId(row.getString(TimeseriesLayout.TAG_DEVICE_ID));
        data.setMessageId(row.getString(TimeseriesLayout.FIELD_MESSAGE_ID));
        data.setDeviceName(row.getString(TimeseriesLayout.FIELD_DEVICE_NAME));
        data.setEvent(row.getString(TimeseriesLayout.TAG_EVENT));
        data.setType(row.getString(TimeseriesLayout.FIELD_TYPE));
        data.setDataType(row.getString(TimeseriesLayout.TAG_DATA_TYPE));
        data.setData(row.getString(TimeseriesLayout.FIELD_DATA));
        data.setTimestamp(row.getLong(TimeseriesLayout.FIELD_CREATE_TIME));
        return data;
    }

    /** 消息日志查询行解码 — 时间为消息上报时间，取消息时间列 create_time（主时间列 _ts 为 TDengine 服务器落库时间，不参与解码） */
    private static MessageLogData messageLogRowToData(TimeseriesRow row) {
        MessageLogData data = new MessageLogData();
        data.setDeviceId(row.getString(TimeseriesLayout.TAG_DEVICE_ID));
        data.setMessageId(row.getString(TimeseriesLayout.FIELD_MESSAGE_ID));
        data.setDeviceName(row.getString(TimeseriesLayout.FIELD_DEVICE_NAME));
        data.setDirection(row.getString(TimeseriesLayout.FIELD_DIRECTION));
        data.setCommandType(row.getString(TimeseriesLayout.TAG_COMMAND_TYPE));
        data.setContent(row.getString(TimeseriesLayout.FIELD_CONTENT));
        data.setTimestamp(row.getLong(TimeseriesLayout.FIELD_CREATE_TIME));
        return data;
    }

    /** 分页结果组装 — 页码/页大小由查询对象分页状态反推（pageSize=limit、pageNum=offset/limit+1，limit<=0 时页大小 0 页数 0） */
    private static <T> PageResult<T> pageResult(TimeseriesQuery tsQuery, long total, List<T> list) {
        long pageSize = tsQuery.getLimit();
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setPageNum(pageSize > 0 ? tsQuery.getOffset() / pageSize + 1 : 1L);
        pageResult.setPageSize(pageSize);
        pageResult.setTotal(total);
        pageResult.setPages(pageSize <= 0 ? 0L : (total + pageSize - 1) / pageSize);
        pageResult.setList(list);
        pageResult.setEmptyFlag(list.isEmpty());
        return pageResult;
    }
}
