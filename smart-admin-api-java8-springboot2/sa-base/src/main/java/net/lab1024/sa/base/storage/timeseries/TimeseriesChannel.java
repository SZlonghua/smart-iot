package net.lab1024.sa.base.storage.timeseries;

import java.util.List;
import java.util.Map;

/**
 * 时序存储通道（后端方言 SPI）— 行协议字符串的物理写入与 SQL 查询执行，屏蔽具体时序库的连接细节
 * （当前唯一实现为 TDengine JDBC 通道 {@link net.lab1024.sa.base.storage.tdengine.TdengineChannel}）。
 * <p>
 * 时序策略族（行/单值列布局）只依赖本接口做物理读写，通道自身方言差异收敛在实现内：
 * 行协议串（LINE 格式）与查询 SQL 为家族内约定（与 TDengine schemaless / InfluxDB LINE 同构），
 * 将来接入方言不同的库（如 InfluxDB 用 Flux/SQL）时由对应策略覆写查询模板或实现等价通道。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
public interface TimeseriesChannel {

    /** 初始化存储（建库/建桶等幂等操作）— 失败仅记录不阻断启动，写入/查询时再暴露 */
    void initStorage();

    /** 批量写入行协议（LINE 格式字符串，measurement = 逻辑表名） */
    void writeLines(List<String> lines);

    /**
     * SQL 查询 — 返回行 Map（键归一化为列名的下划线小写形态，见 {@link TimeseriesLayout#rowKey}；
     * 解码约定按 {@link net.lab1024.sa.base.storage.model.TimeseriesRow} 行键常量取值）；
     * 表/列不存在（产品尚无数据、属性从未上报）→ 返回空列表而非报错，调用方无需判表存在
     */
    List<Map<String, Object>> executeQuery(String sql);
}
