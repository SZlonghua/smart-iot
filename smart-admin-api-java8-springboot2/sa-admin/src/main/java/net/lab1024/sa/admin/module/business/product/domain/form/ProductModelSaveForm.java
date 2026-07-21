package net.lab1024.sa.admin.module.business.product.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ProductModelSaveForm {

    @NotNull(message = "产品ID 不能为空")
    @Schema(description = "产品ID")
    private Long id;

    @Schema(description = "物模型JSON")
    private String modelJson;
}
