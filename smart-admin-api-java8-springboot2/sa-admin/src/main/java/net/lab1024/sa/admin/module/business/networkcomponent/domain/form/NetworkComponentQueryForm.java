package net.lab1024.sa.admin.module.business.networkcomponent.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.base.common.domain.PageParam;
import org.hibernate.validator.constraints.Length;

/**
 * 网络组件 查询表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NetworkComponentQueryForm extends PageParam {

    @Schema(description = "组件名称")
    @Length(max = 64, message = "组件名称最多64字符")
    private String name;

    @Schema(description = "组件类型")
    @Length(max = 32, message = "组件类型最多32字符")
    private String type;
}
