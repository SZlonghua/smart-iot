package net.lab1024.sa.base.storage.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 属性上报业务模型（继承 {@link StorageData} 公共字段）— 一条上报消息对应一个对象。
 * <p>
 * 行形态由所属存储策略决定：row 策略一条消息一行（tags={device_id} + 属性摊平列）；
 * column 策略每个属性一行（tags 加 property + 单值类型列）。属性 Map 业务原样传入，
 * 不做类型转换，值类型决定落列类型（见时序行协议构建）。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PropertyData extends StorageData {

    /** 属性 Map（属性名 → 值；属性名不得占用保留列名 message_id/create_time/device_name，物模型校验上游兜底） */
    private Map<String, Object> properties;
}
