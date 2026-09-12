package net.lab1024.sa.base.storage.model;

import lombok.Data;

/**
 * 属性历史分页查询条件 — 查某个具体属性的历史上报值（property 必填），时间倒序分页，
 * 总条数与当前页数据一次返回（与事件/日志分页同构）。由上层服务构造（productId 需先
 * 经设备上下文解析），交存储策略执行。
 * <p>
 * 收窄路径因行形态不同：row 布局属性为动态列，以 {property} IS NOT NULL 条件收窄
 * （仅含该属性的消息行）；column 布局 property 为 tag，等值条件进 SQL。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Data
public class PropertyHistoryQuery {

    /** 产品 id（决定表名 device_properties_{productId}） */
    private String productId;

    /** 设备 id（tag 等值） */
    private String deviceId;

    /** 属性 id — 必填（历史查询按单个属性分页） */
    private String property;

    /** 起始时间（毫秒，含边界），可空 = 不限制 */
    private Long startTime;

    /** 结束时间（毫秒，含边界），可空 = 不限制 */
    private Long endTime;

    /** 页码（从 1 起） */
    private int pageNum = 1;

    /** 每页条数 */
    private int pageSize = 10;
}
