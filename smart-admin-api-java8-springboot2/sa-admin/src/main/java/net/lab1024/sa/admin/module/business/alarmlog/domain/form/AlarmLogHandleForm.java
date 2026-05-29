package net.lab1024.sa.admin.module.business.alarmlog.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.alarmlog.constant.AlarmStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

import javax.validation.constraints.NotNull;

/**
 * 告警日志 处理表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class AlarmLogHandleForm {

    @Schema(description = "告警ID")
    @NotNull(message = "告警ID不能为空")
    private Long id;

    @SchemaEnum(AlarmStatusEnum.class)
    @CheckEnum(message = "处理状态错误", value = AlarmStatusEnum.class, required = true)
    @NotNull(message = "处理状态不能为空")
    private Integer status;

    @Schema(description = "处理备注")
    private String handleNote;
}
