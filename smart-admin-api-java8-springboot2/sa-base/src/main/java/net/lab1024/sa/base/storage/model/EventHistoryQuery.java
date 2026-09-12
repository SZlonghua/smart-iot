package net.lab1024.sa.base.storage.model;

import lombok.Data;

/**
 * 事件历史分页查询条件 — 由上层服务构造（productId 需先经设备上下文解析），交存储策略执行。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Data
public class EventHistoryQuery {

    /** 产品 id（决定表名 device_events_{productId}） */
    private String productId;

    /** 设备 id（tag 等值） */
    private String deviceId;

    /** 事件标识（id，tag 等值），可空 = 全部事件 */
    private String event;

    /** 起始时间（毫秒，含边界），可空 = 不限制 */
    private Long startTime;

    /** 结束时间（毫秒，含边界），可空 = 不限制 */
    private Long endTime;

    /** 页码（从 1 起） */
    private int pageNum = 1;

    /** 每页条数 */
    private int pageSize = 10;
}
