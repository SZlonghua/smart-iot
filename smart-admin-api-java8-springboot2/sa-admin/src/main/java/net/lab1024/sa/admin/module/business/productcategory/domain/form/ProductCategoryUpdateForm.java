package net.lab1024.sa.admin.module.business.productcategory.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 产品分类 更新表单
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class ProductCategoryUpdateForm {

    @NotNull(message = "分类ID 不能为空")
    @Schema(description = "分类ID")
    private Long id;

    @NotBlank(message = "分类名称 不能为空")
    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "父分类ID")
    private Long parentId;

    @Schema(description = "排序值")
    private Integer sortOrder;

    @Schema(description = "分类描述")
    private String description;
}
