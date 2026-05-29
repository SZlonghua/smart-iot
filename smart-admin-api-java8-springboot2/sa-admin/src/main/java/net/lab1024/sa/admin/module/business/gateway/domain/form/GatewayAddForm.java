package net.lab1024.sa.admin.module.business.gateway.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 设备网关 新建表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class GatewayAddForm {

    @Schema(description = "网关名称", required = true)
    @NotBlank(message = "网关名称不能为空")
    @Length(max = 64, message = "网关名称最多64字符")
    private String name;

    @Schema(description = "网关类型", required = true)
    @NotBlank(message = "网关类型不能为空")
    @Length(max = 32, message = "网关类型最多32字符")
    private String type;

    @Schema(description = "关联网络组件id")
    private Long componentId;

    @Schema(description = "关联协议id")
    private Long protocolId;

    @Schema(description = "传输方式")
    @Length(max = 32, message = "传输方式最多32字符")
    private String transport;

    @Schema(description = "描述")
    @Length(max = 512, message = "描述最多512字符")
    private String description;
}
