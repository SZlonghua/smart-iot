package net.lab1024.sa.base.module.support.protocol.websocket.session;

import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;
import net.lab1024.sa.base.message.codec.DefaultTransport;

/**
 * WebSocket 设备会话 — WebSocket 设备连接。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
public class WebSocketDeviceSession extends AbstractDeviceSession {

    public WebSocketDeviceSession(String deviceId) {
        super(deviceId, DefaultTransport.WebSocket);
    }
}
