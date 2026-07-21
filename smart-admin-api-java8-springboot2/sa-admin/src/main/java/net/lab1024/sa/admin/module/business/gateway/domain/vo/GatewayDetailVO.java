package net.lab1024.sa.admin.module.business.gateway.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.admin.module.business.gateway.constant.TypeEnum;
import net.lab1024.sa.admin.module.business.networkcomponent.domain.vo.NetworkComponentVO;
import net.lab1024.sa.admin.module.business.protocol.domain.vo.ProtocolVO;
import net.lab1024.sa.base.common.swagger.SchemaEnum;

import java.time.LocalDateTime;

/**
 * 设备网关 详情VO
 *
 * @Author 廖涛
 * @Date 2026/06/01
 * @Copyright 1024创新实验室
 */
@Data
public class GatewayDetailVO {

    @Schema(description = "网关id")
    private Long id;

    @Schema(description = "网关名称")
    private String name;

    @SchemaEnum(TypeEnum.class)
    private String type;

    @Schema(description = "传输方式")
    private String transport;

    @Schema(description = "启用状态 1:启用 0:禁用")
    private Integer status;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "网络组件详情")
    private NetworkComponentVO networkComponent;

    @Schema(description = "协议详情")
    private ProtocolVO protocol;
}
