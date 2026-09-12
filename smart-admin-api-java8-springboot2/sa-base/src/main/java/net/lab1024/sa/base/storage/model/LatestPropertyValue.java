package net.lab1024.sa.base.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 单个属性的最新上报值 — 查询行的列值原样（数值/布尔/文本，对象为 JSON 字符串）+ 上报时间毫秒。
 * <p>
 * 由存储策略按自身行形态解析（row：属性列倒序首行；column：单子表 5 类型值列首个非空），
 * 上层服务原样透出，不做类型加工。
 *
 * @Author 廖涛
 * @Date 2026/09/08
 * @Copyright 1024创新实验室
 */
@Data
@AllArgsConstructor
public class LatestPropertyValue {

    /** 属性值（列值原样） */
    private Object value;

    /** 上报时间（毫秒，ts 主时间列） */
    private long timestamp;
}
