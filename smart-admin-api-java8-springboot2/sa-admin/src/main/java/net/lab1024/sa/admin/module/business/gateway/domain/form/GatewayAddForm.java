package net.lab1024.sa.admin.module.business.gateway.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.gateway.constant.TypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 设备网关 新建表单
 *
 * @Author 廖涛
 * @Date 2026/06/01
 * @Copyright 1024创新实验室
 */
@Data
public class GatewayAddForm {

    @Schema(description = "网关名称", required = true)
    @NotBlank(message = "网关名称不能为空")
    @Length(max = 64, message = "网关名称最多64字符")
    private String name;

    @SchemaEnum(desc = "网关类型", value = TypeEnum.class)
    @CheckEnum(value = TypeEnum.class, required = true, message = "网关类型错误")
    @NotBlank(message = "网关类型不能为空")
    private String type;

    @Schema(description = "关联网络组件id")
    private Long componentId;

    @Schema(description = "关联协议id")
    @NotNull(message = "关联协议id不能为空")
    private Long protocolId;

    @Schema(description = "传输方式")
    @Length(max = 32, message = "传输方式最多32字符")
    private String transport;

    @Schema(description = "描述")
    @Length(max = 512, message = "描述最多512字符")
    private String description;
}
