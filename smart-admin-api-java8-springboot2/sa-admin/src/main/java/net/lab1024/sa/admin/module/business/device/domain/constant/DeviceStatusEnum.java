package net.lab1024.sa.admin.module.business.device.domain.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 设备状态 枚举
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@AllArgsConstructor
@Getter
public enum DeviceStatusEnum implements BaseEnum {

    OFFLINE(0, "离线"),
    ONLINE(1, "在线"),
    DISABLED(2, "禁用"),
    ;

    private final Integer value;
    private final String desc;
}
