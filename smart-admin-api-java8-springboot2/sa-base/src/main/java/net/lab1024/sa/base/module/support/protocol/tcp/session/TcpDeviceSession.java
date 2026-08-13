package net.lab1024.sa.base.module.support.protocol.tcp.session;

import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;

/**
 * TCP 设备会话 — 自建 TCP Server 接受设备直连。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
public class TcpDeviceSession extends AbstractDeviceSession {

    private volatile boolean closed;
    private volatile long lastPingTime = System.currentTimeMillis();

    public TcpDeviceSession(String deviceId) {
        super(deviceId, null, DefaultTransport.TCP);
    }

    @Override
    public long lastPingTime() {
        return lastPingTime;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isAlive() {
        return !closed;
    }
}
