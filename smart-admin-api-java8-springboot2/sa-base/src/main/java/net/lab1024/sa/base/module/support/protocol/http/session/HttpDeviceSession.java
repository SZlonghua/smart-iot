package net.lab1024.sa.base.module.support.protocol.http.session;

import net.lab1024.sa.base.device.session.support.AbstractDeviceSession;
import net.lab1024.sa.base.message.codec.DefaultTransport;

/**
 * HTTP 设备会话 — 无连接型，仅追踪最后活跃时间。
 * 不持有 Socket/Channel，无法实时下发消息。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
public class HttpDeviceSession extends AbstractDeviceSession {

    /** 超时时间：120 秒无请求视为离线 */
    private static final long TIMEOUT_MS = 120_000;

    public HttpDeviceSession(String deviceId) {
        super(deviceId, DefaultTransport.HTTP);
    }

    @Override
    public boolean isAlive() {
        return !closed && (System.currentTimeMillis() - lastPingTime) < TIMEOUT_MS;
    }
}
