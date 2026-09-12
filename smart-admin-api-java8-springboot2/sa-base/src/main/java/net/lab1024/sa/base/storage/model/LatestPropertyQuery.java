package net.lab1024.sa.base.storage.model;

import lombok.Data;

import java.util.Collection;

/**
 * 属性最新上报值批量查询条件 — 每个属性独立取最后一条非空上报记录，由上层服务构造（productId 需先
 * 经设备上下文解析），结果按键序为入参顺序（策略返回 LinkedHashMap）。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Data
public class LatestPropertyQuery {

    /** 产品 id（决定表名 device_properties_{productId}） */
    private String productId;

    /** 设备 id（tag 等值） */
    private String deviceId;

    /** 待查属性 id 集合（空集合 = 无查询，返回空结果） */
    private Collection<String> propertyIds;
}
