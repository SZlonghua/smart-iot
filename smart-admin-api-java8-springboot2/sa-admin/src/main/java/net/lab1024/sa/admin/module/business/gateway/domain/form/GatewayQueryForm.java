package net.lab1024.sa.admin.module.business.gateway.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.admin.module.business.gateway.constant.TypeEnum;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.swagger.SchemaEnum;
import net.lab1024.sa.base.common.validator.enumeration.CheckEnum;

/**
 * 设备网关 查询表单
 *
 * @Author 廖涛
 * @Date 2026/06/01
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayQueryForm extends PageParam {

    @Schema(description = "网关名称")
    private String name;

    @SchemaEnum(desc = "网关类型", value = TypeEnum.class)
    @CheckEnum(value = TypeEnum.class, required = false, message = "网关类型错误")
    private String type;
}
