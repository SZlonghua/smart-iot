package net.lab1024.sa.admin.module.business.product.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.product.constant.DeviceTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 产品 更新表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class ProductUpdateForm {

    @NotNull(message = "产品ID 不能为空")
    @Schema(description = "产品ID")
    private Long id;

    @NotBlank(message = "产品名称 不能为空")
    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "产品分类ID")
    private Long categoryId;

    @Schema(description = "产品分类名称")
    private String categoryName;

    @NotBlank(message = "设备类型 不能为空")
    @SchemaEnum(DeviceTypeEnum.class)
    @CheckEnum(value = DeviceTypeEnum.class, required = true, message = "设备类型错误")
    private String deviceType;

    @Schema(description = "产品描述")
    private String description;

    @Schema(description = "物模型JSON")
    private String modelJson;

    @Schema(description = "状态")
    private Integer status;
}
