package net.lab1024.sa.admin.module.business.product.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 设备类型 枚举
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@AllArgsConstructor
@Getter
public enum DeviceTypeEnum implements BaseEnum {

    DIRECT("direct", "直连设备"),
    GATEWAY_CHILD("gateway_child", "网关子设备"),
    GATEWAY("gateway", "网关设备"),
    ;

    private final String value;
    private final String desc;
}
