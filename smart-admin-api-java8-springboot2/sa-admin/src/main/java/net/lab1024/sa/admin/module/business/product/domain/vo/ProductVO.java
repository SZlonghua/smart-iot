package net.lab1024.sa.admin.module.business.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.product.constant.DeviceTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 产品 VO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class ProductVO {

    @Schema(description = "产品ID")
    private Long id;

    @Schema(description = "Product Key")
    private String productKey;

    @Schema(description = "Product Secret")
    private String productSecret;

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "产品分类ID")
    private Long categoryId;

    @Schema(description = "产品分类名称")
    private String categoryName;

    @SchemaEnum(DeviceTypeEnum.class)
    private String deviceType;

    @Schema(description = "产品描述")
    private String description;

    @Schema(description = "物模型JSON")
    private String modelJson;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
