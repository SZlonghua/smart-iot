package net.lab1024.sa.base.device.session.support;

import lombok.Getter;
import lombok.Setter;
import net.lab1024.sa.base.device.DeviceOperator;
import net.lab1024.sa.base.device.session.DeviceSession;
import net.lab1024.sa.base.message.codec.Transport;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 设备会话抽象基类 — 提供公共字段和默认实现。
 * 连接型会话持有 Channel/Connection，无连接型仅追踪时间戳。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
public abstract class AbstractDeviceSession implements DeviceSession {

    protected final String id;
    protected final String deviceId;
    protected final Transport transport;
    protected final long connectTime;

    @Getter
    @Setter
    @Nullable
    protected DeviceOperator operator;

    protected volatile long lastPingTime;
    protected volatile boolean closed;

    protected AbstractDeviceSession(String deviceId, Transport transport) {
        this.id = UUID.randomUUID().toString();
        this.deviceId = deviceId;
        this.transport = transport;
        this.connectTime = System.currentTimeMillis();
        this.lastPingTime = this.connectTime;
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
    public long lastPingTime() {
        return lastPingTime;
    }

    @Override
    public long connectTime() {
        return connectTime;
    }

    @Override
    public Transport getTransport() {
        return transport;
    }

    @Override
    public boolean isAlive() {
        return !closed;
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public Mono<Boolean> isAliveAsync() {
        return Mono.fromSupplier(this::isAlive);
    }

    /** 更新心跳时间 */
    public void ping() {
        this.lastPingTime = System.currentTimeMillis();
    }
}
