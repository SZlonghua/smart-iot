package net.lab1024.sa.admin.module.business.alarmlog.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.alarmlog.constant.AlarmLevelEnum;
import net.lab1024.sa.admin.module.business.alarmlog.constant.AlarmStatusEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 告警日志 VO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class AlarmLogVO {

    @Schema(description = "告警ID")
    private Long id;

    @Schema(description = "设备Key")
    private String deviceKey;

    @SchemaEnum(AlarmLevelEnum.class)
    private Integer level;

    @Schema(description = "告警描述")
    private String description;

    @SchemaEnum(AlarmStatusEnum.class)
    private Integer status;

    @Schema(description = "触发时间")
    private LocalDateTime triggerTime;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理人")
    private String handler;

    @Schema(description = "处理备注")
    private String handleNote;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
