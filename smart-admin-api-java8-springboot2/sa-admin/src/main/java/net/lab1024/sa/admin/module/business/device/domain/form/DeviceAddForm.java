package net.lab1024.sa.admin.module.business.device.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 设备 新建表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class DeviceAddForm {

    @NotBlank(message = "设备名称 不能为空")
    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "父设备ID")
    private Long parentDeviceId;

    @Schema(description = "产品ID")
    private Long productId;
}
