package net.lab1024.sa.admin.module.business.devicelog.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.devicelog.domain.constant.DeviceLogTypeEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备日志 查询表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceLogQueryForm extends PageParam {

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "日志类型（多选，空 = 全部类型）")
    @SchemaEnum(value = DeviceLogTypeEnum.class)
    @CheckEnum(value = DeviceLogTypeEnum.class, message = "日志类型错误")
    private List<String> typeList;

    @Schema(description = "创建时间-开始")
    private LocalDateTime createTimeBegin;

    @Schema(description = "创建时间-结束")
    private LocalDateTime createTimeEnd;
}
