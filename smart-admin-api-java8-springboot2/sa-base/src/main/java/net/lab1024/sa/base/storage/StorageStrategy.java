package net.lab1024.sa.base.storage;

import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.storage.model.CommandLogQuery;
import net.lab1024.sa.base.storage.model.EventData;
import net.lab1024.sa.base.storage.model.EventHistoryQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyQuery;
import net.lab1024.sa.base.storage.model.LatestPropertyValue;
import net.lab1024.sa.base.storage.model.MessageLogData;
import net.lab1024.sa.base.storage.model.PropertyData;
import net.lab1024.sa.base.storage.model.PropertyHistoryQuery;
import net.lab1024.sa.base.storage.model.StorageData;

import java.util.Map;

/**
 * 设备消息数据存储策略接口 — 以业务模型为输入输出的存储领域契约，未来新增存储库（ES / ClickHouse / Hbase / Doris
 * InfluxDB 等）只需实现本接口并在容器注册（注册器自动收集，上层零改动）。
 * <p>
 * 策略即领域服务：落库/查询的语义（消息模型 ↔ 行形态编解码、建表布局、查询方言）全部收敛在策略内部，
 * 上层不感知具体存储库，也不构造任何 SQL / 行数据 —— 查询只传业务条件
 * （{@link PropertyHistoryQuery} / {@link EventHistoryQuery} / {@link CommandLogQuery} /
 * {@link LatestPropertyQuery}，productId 需先经设备上下文解析），结果直接返回业务模型。
 * 实现策略可按需要分中间层抽象（如时序行/时序列策略族共享布局），或直接扁平实现本接口。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
public interface StorageStrategy {

    /** 策略 ID — 与 {@link StoragePolicy} 常量对应（注册器按此收集/查找，产品经存储策略 ID 路由到本策略） */
    String getId();

    /**
     * 消息落库 — 三类业务数据（属性上报 / 事件上报 / 命令日志）的唯一写入口：
     * 入参为抽象父类型 {@link StorageData}，策略按实际子类分派到对应行形态；
     * 存储异常由策略自处理（时序族记日志不阻断），调用方无需感知落库细节
     */
    void save(StorageData data);

    /** 属性历史分页查询 — 查指定属性（property 必填）历史上报值，时间倒序；总条数与当前页数据一次返回 */
    PageResult<PropertyData> queryPropertyHistory(PropertyHistoryQuery query);

    /** 批量查询属性最新上报值 — 每个属性独立取最后一条非空上报记录；结果键 = 属性 id、有序（入参顺序）；无记录属性不返回 */
    Map<String, LatestPropertyValue> queryLatestPropertyValues(LatestPropertyQuery query);

    /** 事件历史分页查询 — 时间倒序；总条数与当前页数据一次返回 */
    PageResult<EventData> queryEventHistory(EventHistoryQuery query);

    /** 命令日志分页查询 — 时间倒序；总条数与当前页数据一次返回 */
    PageResult<MessageLogData> queryCommandLogHistory(CommandLogQuery query);
}
