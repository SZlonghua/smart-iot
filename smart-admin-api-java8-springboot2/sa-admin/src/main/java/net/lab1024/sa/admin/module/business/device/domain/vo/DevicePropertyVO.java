package net.lab1024.sa.admin.module.business.device.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备属性值 VO
 *
 * @Author 廖涛
 * @Date 2026/06/14
 * @Copyright 1024创新实验室
 */
@Data
public class DevicePropertyVO {

    @Schema(description = "属性标识")
    private String propertyId;

    @Schema(description = "属性名称")
    private String propertyName;

    @Schema(description = "属性值")
    private String formatValue;

    @Schema(description = "更新时间")
    private LocalDateTime timeValue;
}
