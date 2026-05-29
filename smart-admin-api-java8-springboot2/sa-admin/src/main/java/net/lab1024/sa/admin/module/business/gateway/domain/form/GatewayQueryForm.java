package net.lab1024.sa.admin.module.business.gateway.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;
import org.hibernate.validator.constraints.Length;

/**
 * 设备网关 查询表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GatewayQueryForm extends PageParam {

    @Schema(description = "网关名称")
    @Length(max = 64, message = "网关名称最多64字符")
    private String name;

    @Schema(description = "网关类型")
    @Length(max = 32, message = "网关类型最多32字符")
    private String type;

    @Schema(description = "传输方式")
    @Length(max = 32, message = "传输方式最多32字符")
    private String transport;
}
