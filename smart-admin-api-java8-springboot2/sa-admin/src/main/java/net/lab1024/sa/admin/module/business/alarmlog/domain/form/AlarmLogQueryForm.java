package net.lab1024.sa.admin.module.business.alarmlog.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.alarmlog.constant.AlarmLevelEnum;
import net.lab1024.sa.admin.module.business.alarmlog.constant.AlarmStatusEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

import java.time.LocalDateTime;

/**
 * 告警日志 查询表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmLogQueryForm extends PageParam {

    @Schema(description = "设备Key")
    private String deviceKey;

    @SchemaEnum(AlarmLevelEnum.class)
    @CheckEnum(message = "告警级别错误", value = AlarmLevelEnum.class, required = false)
    private Integer level;

    @SchemaEnum(AlarmStatusEnum.class)
    @CheckEnum(message = "处理状态错误", value = AlarmStatusEnum.class, required = false)
    private Integer status;

    @Schema(description = "触发时间-开始")
    private LocalDateTime triggerTimeBegin;

    @Schema(description = "触发时间-结束")
    private LocalDateTime triggerTimeEnd;
}
