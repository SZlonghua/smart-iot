package net.lab1024.sa.base.device.session.support;

import lombok.Getter;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.common.message.codec.Transport;

/**
 * 子设备会话 — 不持有实际网络连接。
 * 通过网关代理通信，alive 状态跟随父设备（网关）会话。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
public class ChildrenDeviceSession extends AbstractDeviceSession {

    /** 网关会话（连通性依赖） */
    @Getter
    private final DeviceSession parent;

    public ChildrenDeviceSession(String deviceId, Transport transport,
                                  DeviceOperator operator, DeviceSession parent) {
        super(deviceId, operator, transport);
        this.parent = parent;
    }

    @Override
    public long lastPingTime() {
        return 0;
    }

    @Override
    public void close() {

    }

    @Override
    public boolean isAlive() {
        return parent != null && parent.isAlive();
    }
}
