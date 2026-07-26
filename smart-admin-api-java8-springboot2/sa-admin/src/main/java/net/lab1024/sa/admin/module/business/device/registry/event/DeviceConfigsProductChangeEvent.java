package net.lab1024.sa.admin.module.business.device.registry.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 产品配置变更事件 — 产品信息变更时发布，通知 ProductChangeSyncHandler 同步设备冗余字段。
 *
 * @Author 廖涛
 * @Date 2026/07/25
 * @Copyright 1024创新实验室
 */
@Getter
@ToString
@AllArgsConstructor
public class DeviceConfigsProductChangeEvent {

    private final Long productId;
    private final String productName;
    private final String productKey;
    private final String productSecret;
}
