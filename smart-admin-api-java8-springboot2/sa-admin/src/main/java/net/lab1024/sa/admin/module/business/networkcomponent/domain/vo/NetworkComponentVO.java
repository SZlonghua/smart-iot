package net.lab1024.sa.admin.module.business.networkcomponent.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.networkcomponent.constant.ComponentTypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 网络组件 VO
 *
 * @Author 廖涛
 * @Date 2026/05/26
 * @Copyright 1024创新实验室
 */
@Data
public class NetworkComponentVO {

    @Schema(description = "组件id")
    private Long id;

    @Schema(description = "组件名称")
    private String name;

    @SchemaEnum(ComponentTypeEnum.class)
    private String type;

    @Schema(description = "组件配置(JSON)")
    private String configuration;

    @Schema(description = "启用状态 1:启用 0:禁用")
    private Integer status;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
