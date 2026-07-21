package net.lab1024.sa.admin.module.business.device.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 设备属性操作表单
 *
 * @Author 廖涛
 * @Date 2026/06/12
 * @Copyright 1024创新实验室
 */
@Data
public class DevicePropertyForm {

    @NotNull(message = "设备ID 不能为空")
    @Schema(description = "设备ID")
    private Long deviceId;

    @NotBlank(message = "属性ID 不能为空")
    @Schema(description = "属性ID")
    private String propertyId;

    @Schema(description = "属性值")
    private String value;
}
