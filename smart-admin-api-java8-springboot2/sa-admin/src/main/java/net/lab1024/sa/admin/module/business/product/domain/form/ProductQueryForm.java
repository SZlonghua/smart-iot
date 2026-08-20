package net.lab1024.sa.admin.module.business.product.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.product.constant.DeviceTypeEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

/**
 * 产品 查询表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProductQueryForm extends PageParam {

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "Product Key")
    private String productKey;

    @Schema(description = "产品分类ID")
    private Long categoryId;

    @SchemaEnum(DeviceTypeEnum.class)
    @CheckEnum(value = DeviceTypeEnum.class, required = false, message = "设备类型错误")
    private String deviceType;

    @Schema(description = "状态(0禁用 1启用)，设备新建选产品时传1只查已启用")
    private Integer status;
}
