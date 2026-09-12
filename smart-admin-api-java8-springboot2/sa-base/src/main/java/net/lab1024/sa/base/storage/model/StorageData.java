package net.lab1024.sa.base.storage.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 设备消息数据模型抽象父类 — 属性上报 / 事件上报 / 命令日志三类消息的公共字段。
 * <p>
 * 存储策略对业务模型的统一入口为 {@code save(StorageData)}：策略层按实际子类分派到对应行形态落库，
 * 不做 instanceof 之外的业务判断；全字段由消息存储监听器转换时一次填齐（productId/deviceName 取设备
 * 上下文回填），策略层不触碰设备上下文。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Getter
@Setter
public abstract class StorageData {

    /** 设备 id（tag，区分产品下设备） */
    private String deviceId;

    /** 产品 id（决定表名 device_xxx_{productId}，监听器转换时回填） */
    private String productId;

    /** 设备名称（冗余字段，取设备自配置 DEVICE_NAME 快照，便于展示） */
    private String deviceName;

    /** 消息 id */
    private String messageId;

    /** 消息时间（毫秒）— 监听器取消息报文 timestamp 填充并落 create_time 列；读取时由该列解码回填 */
    private long timestamp;
}
