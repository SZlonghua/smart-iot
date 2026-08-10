package net.lab1024.sa.base.module.support.protocol.websocket.session;

import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;
import net.lab1024.sa.base.common.message.codec.DefaultTransport;

/**
 * WebSocket 设备会话 — WebSocket 设备连接。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
public class WebSocketDeviceSession extends AbstractDeviceSession {

    private volatile boolean closed;
    private volatile long lastPingTime = System.currentTimeMillis();

    public WebSocketDeviceSession(String deviceId) {
        super(deviceId, null, DefaultTransport.WebSocket);
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
