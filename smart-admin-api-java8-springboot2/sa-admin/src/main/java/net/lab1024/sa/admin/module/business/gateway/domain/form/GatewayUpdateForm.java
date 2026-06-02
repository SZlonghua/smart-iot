package net.lab1024.sa.admin.module.business.gateway.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 设备网关 更新表单
 *
 * @Author 廖涛
 * @Date 2026/06/01
 * @Copyright 1024创新实验室
 */
@Data
public class GatewayUpdateForm extends GatewayAddForm {

    @Schema(description = "网关id", required = true)
    @NotNull(message = "网关id不能为空")
    private Long id;
}
