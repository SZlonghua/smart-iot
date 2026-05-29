package net.lab1024.sa.admin.module.business.alarmlog.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 告警级别 枚举
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@AllArgsConstructor
@Getter
public enum AlarmLevelEnum implements BaseEnum {

    EMERGENCY(1, "紧急"),

    IMPORTANT(2, "重要"),

    MINOR(3, "次要"),

    WARNING(4, "提示"),

    ;

    private final Integer value;

    private final String desc;
}
