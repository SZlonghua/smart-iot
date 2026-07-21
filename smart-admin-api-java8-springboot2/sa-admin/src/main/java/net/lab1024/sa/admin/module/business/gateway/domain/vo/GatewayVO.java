package net.lab1024.sa.admin.module.business.gateway.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.gateway.constant.TypeEnum;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 设备网关 VO
 *
 * @Author 廖涛
 * @Date 2026/06/01
 * @Copyright 1024创新实验室
 */
@Data
public class GatewayVO {

    @Schema(description = "网关id")
    private Long id;

    @Schema(description = "网关名称")
    private String name;

    @SchemaEnum(TypeEnum.class)
    private String type;

    @Schema(description = "关联网络组件id")
    private Long componentId;

    @Schema(description = "网络组件名称")
    private String componentName;

    @Schema(description = "关联协议id")
    private Long protocolId;

    @Schema(description = "协议名称")
    private String protocolName;

    @Schema(description = "启用状态 1:启用 0:禁用")
    private Integer status;

    @Schema(description = "传输方式")
    private String transport;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
