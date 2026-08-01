package net.lab1024.sa.base.common.gateway;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Generated
public class DeviceGatewayProperties {
    private String id;

    private String name;

    private String description;
    // 对应type字段
    private String provider;

    private String protocol;

    private String transport;

    /** 关联网络组件id */
    private String componentId;

    /** 启用状态 1:启用 0:禁用 */
    private Integer status;

    public boolean isEnabled() {
        return status != null && status == 1;
    }

}
