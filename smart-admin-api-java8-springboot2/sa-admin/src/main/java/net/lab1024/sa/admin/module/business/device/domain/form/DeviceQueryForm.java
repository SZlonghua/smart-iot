package net.lab1024.sa.admin.module.business.device.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;

/**
 * 设备 查询表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DeviceQueryForm extends PageParam {

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "Device Key")
    private String deviceKey;

    @Schema(description = "产品ID")
    private Long productId;

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "设备网关ID")
    private Long gatewayId;
}
