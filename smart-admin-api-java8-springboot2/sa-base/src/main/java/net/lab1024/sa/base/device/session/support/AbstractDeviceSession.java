package net.lab1024.sa.base.device.session.support;

import lombok.Setter;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.common.message.codec.Transport;

import javax.annotation.Nullable;

/**
 * 设备会话抽象基类 — 提供公共字段和默认实现。
 * 连接型会话持有 Channel/Connection，无连接型仅追踪时间戳。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/07/27
 * &#064;Copyright  1024创新实验室
 */
public abstract class AbstractDeviceSession implements DeviceSession {

    protected final String id;
    protected final String deviceId;
    protected final Transport transport;
    protected final long connectTime;

    @Setter
    @Nullable
    protected DeviceOperator operator;

    protected AbstractDeviceSession(String deviceId, @Nullable DeviceOperator deviceOperator, Transport transport) {
        this.id = deviceId;
        this.deviceId = deviceId;
        this.transport = transport;
        this.operator = deviceOperator;
        this.connectTime = System.currentTimeMillis();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    @Nullable
    public DeviceOperator getOperator() {
        return operator;
    }

    @Override
    public long connectTime() {
        return connectTime;
    }

    @Override
    public Transport getTransport() {
        return transport;
    }


}
