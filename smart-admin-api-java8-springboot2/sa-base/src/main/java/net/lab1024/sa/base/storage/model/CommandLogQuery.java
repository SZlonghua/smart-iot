package net.lab1024.sa.base.storage.model;

import lombok.Data;

import java.util.List;

/**
 * 命令日志分页查询条件 — 由上层服务构造（productId 需先经设备上下文解析），交存储策略执行。
 * <p>
 * commandTypes 为 TopicMessageCodec 枚举名集合（tag IN）；direction 为字段列等值；
 * deviceName 为冗余字段精确匹配；三者可空 = 不按该条件过滤。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Data
public class CommandLogQuery {

    /** 产品 id（决定表名 device_message_logs_{productId}） */
    private String productId;

    /** 设备 id（tag 等值） */
    private String deviceId;

    /** 命令类型集合（tag IN，类型与回复类型成对传入），可空 = 全部命令类型 */
    private List<String> commandTypes;

    /** 方向 up/down（字段列等值），可空 = 不限制 */
    private String direction;

    /** 设备名称（冗余字段精确匹配），可空 = 不限制 */
    private String deviceName;

    /** 起始时间（毫秒，含边界），可空 = 不限制 */
    private Long startTime;

    /** 结束时间（毫秒，含边界），可空 = 不限制 */
    private Long endTime;

    /** 页码（从 1 起） */
    private int pageNum = 1;

    /** 每页条数 */
    private int pageSize = 10;
}
