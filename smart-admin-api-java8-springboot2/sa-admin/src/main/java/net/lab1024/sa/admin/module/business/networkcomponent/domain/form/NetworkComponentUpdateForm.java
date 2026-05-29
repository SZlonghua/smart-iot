package net.lab1024.sa.admin.module.business.networkcomponent.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 网络组件 更新表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class NetworkComponentUpdateForm extends NetworkComponentAddForm {

    @Schema(description = "组件id", required = true)
    @NotNull(message = "组件id不能为空")
    private Long id;
}
