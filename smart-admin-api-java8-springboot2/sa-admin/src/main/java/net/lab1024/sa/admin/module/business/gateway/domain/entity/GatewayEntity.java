package net.lab1024.sa.admin.module.business.gateway.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.lab1024.sa.admin.module.business.gateway.constant.TypeEnum;
import net.lab1024.sa.base.common.util.SmartEnumUtil;

import java.time.LocalDateTime;

/**
 * 设备网关 实体类
 *
 * @Author 廖涛
 * @Date 2026/06/01
 * @Copyright 1024创新实验室
 */
@Data
@TableName("gateway")
public class GatewayEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 网关名称 */
    private String name;

    /** 网关类型 */
    private String type;

    /** 关联网络组件id */
    private Long componentId;

    /** 关联协议id */
    private Long protocolId;

    /** 传输方式 */
    private String transport;

    /** 启用状态 1:启用 0:禁用 */
    private Integer status;

    /** 描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否启用（0=禁用，新增时null视为未禁用，DB默认1启用） */
    public boolean isEnabled() {
        return status == null || status != 0;
    }

    public net.lab1024.sa.base.common.gateway.DeviceGatewayProperties toProperties() {
        net.lab1024.sa.base.common.gateway.DeviceGatewayProperties p =
                new net.lab1024.sa.base.common.gateway.DeviceGatewayProperties();
        p.setId(String.valueOf(id));
        p.setName(name);
        p.setDescription(description);
        p.setProvider(getProviderId());
        p.setProtocol(String.valueOf(protocolId));
        p.setTransport(transport);
        p.setComponentId(String.valueOf(componentId));
        p.setStatus(status);
        return p;
    }

    private String getProviderId() {
        return SmartEnumUtil.getEnumByValue(type, TypeEnum.class)
                .getProviderId();
    }
}
