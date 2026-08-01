package net.lab1024.sa.base.common.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备网关生命周期事件。
 *
 * @Author 廖涛
 * @Date 2026/07/31
 * @Copyright 1024创新实验室
 */
@Getter
@AllArgsConstructor(staticName = "of")
public class DeviceGatewayEvent {

    private final Type type;
    private final DeviceGatewayProperties properties;

    public enum Type {
        register,
        reload,
        unregister
    }
}
