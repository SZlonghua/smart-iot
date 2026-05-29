package net.lab1024.sa.admin.module.business.networkcomponent.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.networkcomponent.constant.ComponentTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 网络组件 新建表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class NetworkComponentAddForm {

    @Schema(description = "组件名称", required = true)
    @NotBlank(message = "组件名称不能为空")
    @Length(max = 64, message = "组件名称最多64字符")
    private String name;

    @SchemaEnum(desc = "组件类型", value = ComponentTypeEnum.class)
    @CheckEnum(value = ComponentTypeEnum.class, required = true, message = "组件类型错误")
    @NotBlank(message = "组件类型不能为空")
    private String type;

    @Schema(description = "组件配置(JSON)")
    private String configuration;

    @Schema(description = "启用状态 1:启用 0:禁用")
    private Integer status;

    @Schema(description = "描述")
    @Length(max = 512, message = "描述最多512字符")
    private String description;
}
