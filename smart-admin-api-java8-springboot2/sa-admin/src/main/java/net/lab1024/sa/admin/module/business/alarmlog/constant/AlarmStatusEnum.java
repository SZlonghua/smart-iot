package net.lab1024.sa.admin.module.business.alarmlog.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

/**
 * 告警处理状态 枚举
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@AllArgsConstructor
@Getter
public enum AlarmStatusEnum implements BaseEnum {

    PENDING(1, "未处理"),

    PROCESSING(2, "处理中"),

    RESOLVED(3, "已处理"),

    IGNORED(4, "已忽略"),

    ;

    private final Integer value;

    private final String desc;
}
